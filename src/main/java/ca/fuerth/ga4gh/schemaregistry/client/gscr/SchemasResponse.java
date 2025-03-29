package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SchemasResponse(
    @NotBlank String namespace,
    List<SchemaRecord> schemas,
    List<SchemaRecord> results) {

    /**
     * Temporary workaround while we decide if responses should have "results" uniformly at the root.
     */
    @NotNull
    @Override
    public List<SchemaRecord> schemas() {
        if (schemas != null) {
            return schemas;
        }
        return results;
    }

}
