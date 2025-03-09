package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import lombok.Data;

public record Namespace(
    String namespaceName,
    String contactUrl) {
}
