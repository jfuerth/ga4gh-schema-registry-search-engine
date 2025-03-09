package ca.fuerth.ga4gh.schemaregistry.crawler;

import ca.fuerth.ga4gh.schemaregistry.client.gscr.GscrClient;
import ca.fuerth.ga4gh.schemaregistry.client.gscr.Namespace;
import ca.fuerth.ga4gh.schemaregistry.client.gscr.NamespacesResponse;
import ca.fuerth.ga4gh.schemaregistry.client.gscr.SchemasResponse;
import ca.fuerth.ga4gh.schemaregistry.util.Env;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import static ca.fuerth.ga4gh.schemaregistry.util.Env.requiredEnv;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class CrawlerE2eTest {

    @Autowired
    Crawler crawler;

    @Test
    void crawl_should_findAllSchemasInAllNamespaces() {
        URI registryBaseUri = URI.create(requiredEnv("E2E_REGISTRY_BASE_URI"));

        List<Namespace> namespaces = List.of(
                new Namespace("namespace-1", "mailto:namespace1@example.com"),
                new Namespace("namespace-2", "mailto:namespace2@example.com")
        );

        List<SchemasResponse> ns1Schemas = List.of();

        Stream<IndexableSchema> indexableSchemas = crawler.crawl(registryBaseUri, s -> !s.equals("dbGaP"));
        assertThat(indexableSchemas).isNotNull();
        indexableSchemas.forEach(System.out::println);
    }
}