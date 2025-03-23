package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import java.util.List;

public record SchemasResponse(
    String namespace,
    List<SchemaRecord> schemas,
    List<SchemaRecord> results) {
    @Override
    public List<SchemaRecord> schemas() {
        if (schemas != null) {
            return schemas;
        }
        return results;
    }

}
