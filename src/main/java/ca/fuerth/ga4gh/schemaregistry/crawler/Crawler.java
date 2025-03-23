package ca.fuerth.ga4gh.schemaregistry.crawler;

import ca.fuerth.ga4gh.schemaregistry.client.gscr.GscrClient;
import ca.fuerth.ga4gh.schemaregistry.client.gscr.Namespace;
import ca.fuerth.ga4gh.schemaregistry.client.gscr.SchemaRecord;
import ca.fuerth.ga4gh.schemaregistry.client.gscr.SchemasResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
public class Crawler {

    @Autowired
    private GscrClient gscrClient;

    public Stream<CrawledSchema> crawl(URI registryBaseUri, Predicate<String> namespaceFilter) {
        Map<String, Namespace> namespaces = gscrClient.getNamespaces(registryBaseUri).namespaces().stream()
                .collect(toMap(Namespace::namespaceName, Function.identity()));

        log.debug("Found {} namespaces: {}", namespaces.size(), namespaces.values());

        return namespaces.keySet().stream()
                .filter(namespaceFilter)
                .map(ns -> gscrClient.getSchemas(registryBaseUri, ns))
                .peek(Crawler::logSchemasResponse)
                .flatMap(namespaceSchemas -> {
                    // Just fetching the latest version of each schema for now
                    // TODO allow fetching all/older versions
                    return namespaceSchemas.schemas().stream()
                            .map(schema ->
                                    new CrawledSchema(
                                            registryBaseUri,
                                            namespaces.get(namespaceSchemas.namespace()),
                                            schema,
                                            gscrClient.computeSchemaVersionUri(
                                                    registryBaseUri,
                                                    namespaceSchemas.namespace(),
                                                    schema.schemaName(),
                                                    GscrClient.LATEST_VERSION),
                                            gscrClient.getJsonSchemaAsString(
                                                    registryBaseUri,
                                                    namespaceSchemas.namespace(),
                                                    schema.schemaName(),
                                                    GscrClient.LATEST_VERSION)));
                })
                .peek(indexableSchema -> log.debug("Found {}", indexableSchema));
    }


    private static void logSchemasResponse(SchemasResponse schemasPerNamespace) {
        for (SchemaRecord schema : schemasPerNamespace.schemas()) {
            log.debug("Namespace {}: Schema {}", schemasPerNamespace.namespace(), schema);
        }
    }
}
