package ca.fuerth.ga4gh.schemaregistry.crawler;

import ca.fuerth.ga4gh.schemaregistry.client.gscr.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrawlerTest {

    @Mock
    GscrClient gscrClient;

    @InjectMocks
    Crawler crawler;

    /** The registry URI the test data claims to be from */
    URI registryBaseUri = URI.create("https://schemas.example.com");

    @BeforeEach
    void setupTestData() {

        List<Namespace> namespaces = List.of(
                new Namespace("namespace-1", "mailto:namespace1@example.com"),
                new Namespace("namespace-2", "mailto:namespace2@example.com"),
                new Namespace("namespace-empty", "mailto:namespace-empty@example.com")
        );

        SchemasResponse ns1Schemas = new SchemasResponse("namespace-1", List.of(
                new SchemaRecord("sch1", "1.0.0", List.of("person 1", "person 2"), "draft"),
                new SchemaRecord("sch2", "1.0.0", List.of("person 3", "person 4"), "draft")
        ));
        SchemasResponse ns2Schemas = new SchemasResponse("namespace-2", List.of(
                new SchemaRecord("sch3", "1.0.0", List.of("person 5", "person 6"), "draft")
        ));
        SchemasResponse nsEmptySchemas = new SchemasResponse("namespace-empty", List.of());

        when(gscrClient.getNamespaces(registryBaseUri)).thenReturn(new NamespacesResponse(registryBaseUri, namespaces));
        lenient().when(gscrClient.getSchemas(registryBaseUri, "namespace-1")).thenReturn(ns1Schemas);
        lenient().when(gscrClient.getSchemas(registryBaseUri, "namespace-2")).thenReturn(ns2Schemas);
        lenient().when(gscrClient.getSchemas(registryBaseUri, "namespace-empty")).thenReturn(nsEmptySchemas);
    }

    @Test
    void crawl_should_findAllSchemasInAllNamespaces() {
        Stream<IndexableSchema> schemaStream = crawler.crawl(registryBaseUri, s -> true);
        assertThat(schemaStream).isNotNull();
        List<IndexableSchema> schemas = schemaStream.toList();

        assertThat(schemas).hasSize(3);
    }

    @Test
    void crawl_should_returnEmptyStream_when_allNamespacesAreFilteredOut() {
        Stream<IndexableSchema> schemaStream = crawler.crawl(registryBaseUri, s -> false);
        assertThat(schemaStream).isNotNull();
        List<IndexableSchema> schemas = schemaStream.toList();

        assertThat(schemas).isEmpty();
    }

    @Test
    void crawl_should_omitFilteredNamespaces() {
        Stream<IndexableSchema> schemaStream = crawler.crawl(registryBaseUri, s -> s.equals("namespace-2"));
        assertThat(schemaStream).isNotNull();
        List<IndexableSchema> schemas = schemaStream.toList();

        assertThat(schemas)
                .extracting(IndexableSchema::namespace)
                .extracting(Namespace::namespaceName)
                .containsOnly("namespace-2");
    }
}