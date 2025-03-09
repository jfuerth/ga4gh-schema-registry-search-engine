package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import feign.Param;
import feign.RequestLine;
import feign.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public interface GscrClient {

    /**
     * Special value to request the latest version. Use in place of a literal semantic version.
     */
    String LATEST_VERSION = "latest";

    @RequestLine("GET /namespaces")
    NamespacesResponse getNamespaces(URI registryBaseUri);

    @RequestLine("GET /schemas/{namespace}/")
        // TODO remove trailing slash when server is updated
    SchemasResponse getSchemas(URI registryBaseUri,
                               @Param("namespace") String namespace);

    @RequestLine("GET /schemas/{namespace}?schema_name={schema_name}")
    SchemasResponse getSchema(URI registryBaseUri,
                              @Param("namespace") String namespace,
                              @Param("schema_name") String schemaName);

    @RequestLine("GET /schemas/{namespace}/{schema_name}/versions")
    SchemaVersionsResponse getSchemaVersions(URI registryBaseUri,
                                             @Param("namespace") String namespace,
                                             @Param("schema_name") String schemaName);

    // TODO (spec) there should be a way to get the version metadata of a single version
    // use case: I want to retrieve it and index it
    // maybe these paths:
    // GET /schemas/{namespace}/{schema_name}/versions/{semantic_version}/info
    // GET /schemas/{namespace}/{schema_name}/versions/{semantic_version}/schema

    @RequestLine("GET /schemas/{namespace}/{schema_name}/versions/{semantic_version}")
    Response getJsonSchema(URI registryBaseUri,
                           @Param("namespace") String namespace,
                           @Param("schema_name") String schemaName,
                           @Param("semantic_version") String semanticVersion);

    default String getJsonSchemaAsString(URI registryBaseUri,
                                         @Param("namespace") String namespace,
                                         @Param("schema_name") String schemaName,
                                         @Param("semantic_version") String semanticVersion) {
        try (Response r = getJsonSchema(registryBaseUri, namespace, schemaName, semanticVersion);
             InputStream in = r.body().asInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
