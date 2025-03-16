package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Metadata specific to a version of a schema. */
public record SchemaVersion(
    String version,
    String status,
    LocalDateTime releaseDate,
    List<String> contributors,
    String releaseNotes,
    Map<String, String> tags) {
}
