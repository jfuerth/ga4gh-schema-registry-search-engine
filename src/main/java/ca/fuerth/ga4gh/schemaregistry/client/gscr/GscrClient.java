package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import feign.Param;
import feign.RequestLine;
import feign.Response;
import feign.template.UriTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public interface GscrClient {

    /**
     * Special value to request the latest version. Use in place of a literal semantic version.
     */
    String LATEST_VERSION = "latest";

    @RequestLine("GET /namespaces")
    NamespacesResponse getNamespaces(URI registryBaseUri);

    @RequestLine("GET /schemas/{namespace}")
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

    default URI computeSchemaVersionUri(URI baseUri, String namespace, String schemaName, String semanticVersion) {
        UriTemplate uriTemplate = UriTemplate.create("/schemas/{namespace}/{schema_name}/versions/{semantic_version}", StandardCharsets.UTF_8);
        return baseUri.resolve(uriTemplate.expand(Map.of(
                "base_uri", baseUri.toString(),
                "namespace", requireNonNull(namespace, "namespace is null"),
                "schema_name", requireNonNull(schemaName, "schema_name is null"),
                "semantic_version", requireNonNull(semanticVersion, "semantic_version is null"))));
    }

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
