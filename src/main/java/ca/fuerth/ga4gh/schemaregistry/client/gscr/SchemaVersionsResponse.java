package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SchemaVersionsResponse(
    @NotBlank String schemaName,
    List<SchemaVersion> versions,
    List<SchemaVersion> results) {

    /**
     * Temporary workaround while we decide if responses should have "results" uniformly at the root.
     */
    @NotNull
    @Override
    public List<SchemaVersion> versions() {
        if (versions != null) {
            return versions;
        }
        return results;
    }

}
