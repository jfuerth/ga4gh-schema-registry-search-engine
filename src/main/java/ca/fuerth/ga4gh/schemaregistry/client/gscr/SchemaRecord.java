package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/** Metadata about a schema which is the same across all versions. */
public record SchemaRecord( // TODO (spec) it would be really handy to have the namespace name here
    @NotBlank String schemaName,
    @NotBlank String latestReleasedVersion,
    List<String> maintainer,
    @NotBlank String maturityLevel) {
}
