package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import jakarta.validation.constraints.NotBlank;

public record Namespace(
    @NotBlank String namespaceName,
    String contactUrl) {
}
