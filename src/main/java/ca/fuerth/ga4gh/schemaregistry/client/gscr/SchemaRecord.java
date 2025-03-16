package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import java.util.List;

/** Metadata about a schema which is the same across all versions. */
public record SchemaRecord(
    String schemaName,
    String latestReleasedVersion,
    List<String> maintainer,
    String maturityLevel) {
}
