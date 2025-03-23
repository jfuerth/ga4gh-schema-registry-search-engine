package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import java.util.List;

public record SchemaVersionsResponse(
    String schemaName,
    List<SchemaVersion> versions,
    List<SchemaVersion> results) {
    @Override
    public List<SchemaVersion> versions() {
        if (versions != null) {
            return versions;
        }
        return results;
    }

}
