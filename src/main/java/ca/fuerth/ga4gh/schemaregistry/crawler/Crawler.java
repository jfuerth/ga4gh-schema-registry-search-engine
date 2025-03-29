package ca.fuerth.ga4gh.schemaregistry.crawler;

import ca.fuerth.ga4gh.schemaregistry.client.gscr.GscrClient;
import ca.fuerth.ga4gh.schemaregistry.client.gscr.Namespace;
import ca.fuerth.ga4gh.schemaregistry.client.gscr.SchemaRecord;
import ca.fuerth.ga4gh.schemaregistry.client.gscr.SchemasResponse;
import ca.fuerth.ga4gh.schemaregistry.shared.FailableResult;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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

    public Stream<FailableResult<CrawledSchema>> crawl(URI registryBaseUri, Predicate<String> namespaceFilter) {
        // TODO make this part of the FailableResult chain so we get a good error when it fails
        Map<String, Namespace> namespaces = gscrClient.getNamespaces(registryBaseUri).namespaces().stream()
                .collect(toMap(Namespace::namespaceName, Function.identity()));

        log.debug("Found {} namespaces: {}", namespaces.size(), namespaces.values());

        return namespaces.keySet().stream()
                .filter(namespaceFilter)
                .map(ns -> FailableResult.of(
                        "getting namespace " + ns,
                        () -> gscrClient.getSchemas(registryBaseUri, ns)))
                .peek(Crawler::logSchemasResponse)
                .flatMap(Crawler::expandToSchemaStream)
                .map(fSchema -> downloadJsonSchema(registryBaseUri, fSchema, namespaces))
                .peek(crawledSchema -> log.debug("Hydrated with JSON Schema {}", crawledSchema));
    }

    @NotNull
    private static Stream<FailableResult<NamespaceSchema>> expandToSchemaStream(FailableResult<SchemasResponse> fSchemasResponse) {
        return fSchemasResponse.flatten(
                schemasResponse -> "iterating schemas of namespace " + schemasResponse.namespace(),
                schemasResponse -> schemasResponse.schemas().stream()
                        .map(s -> new NamespaceSchema(schemasResponse.namespace(), s)));
    }

    @NotNull
    private FailableResult<CrawledSchema> downloadJsonSchema(URI registryBaseUri, FailableResult<NamespaceSchema> fSchema, Map<String, Namespace> namespaces) {
        // Just fetching the latest version of each schema for now
        // TODO allow fetching all/older versions
        return fSchema.map(
                failedSchema -> "fetching JSON schema %s/%s ".formatted(failedSchema.namespace(), failedSchema.schemaRecord().schemaName()),
                schema -> retrieveSchema(registryBaseUri, schema, namespaces));
    }

    // TODO this is just a workaround for the SchemaRecord not having the namespace in it
    record NamespaceSchema(String namespace, SchemaRecord schemaRecord) {}

    @NotNull
    private CrawledSchema retrieveSchema(URI registryBaseUri, NamespaceSchema schema, Map<String, Namespace> namespaces) {
        return new CrawledSchema(
                registryBaseUri,
                namespaces.get(schema.namespace()),
                schema.schemaRecord(),
                gscrClient.computeSchemaVersionUri(
                        registryBaseUri,
                        schema.namespace(),
                        schema.schemaRecord().schemaName(),
                        GscrClient.LATEST_VERSION),
                gscrClient.getJsonSchemaAsString(
                        registryBaseUri,
                        schema.namespace(),
                        schema.schemaRecord().schemaName(),
                        GscrClient.LATEST_VERSION));
    }


    private static void logSchemasResponse(FailableResult<SchemasResponse> schemasPerNamespace) {
        if (schemasPerNamespace.isSuccess()) {
            for (SchemaRecord schema : schemasPerNamespace.result().schemas()) {
                log.debug("Namespace {}: Schema {}", schemasPerNamespace.result().namespace(), schema);
            }
        }
    }
}
