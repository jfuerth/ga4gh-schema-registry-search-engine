package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import lombok.Data;

import java.util.List;

public record SchemaVersionsResponse(
    String schemaName,
    List<SchemaVersion> versions) {
}
