package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Metadata specific to a version of a schema. */
public record SchemaVersion(
    @NotBlank String version,
    String status,
    LocalDateTime releaseDate,
    @NotNull List<String> contributors,
    String releaseNotes,
    Map<String, String> tags) {
}
