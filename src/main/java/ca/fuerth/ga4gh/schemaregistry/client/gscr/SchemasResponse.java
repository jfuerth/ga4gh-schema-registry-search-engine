package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import java.util.List;

public record SchemasResponse(
    String namespace,
    List<SchemaRecord> schemas) {
}
