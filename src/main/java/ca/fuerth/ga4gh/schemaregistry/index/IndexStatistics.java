package ca.fuerth.ga4gh.schemaregistry.index;

public record IndexStatistics(
        int registryCount,
        int schemaCount,
        int schemaFragmentCount
) {
}
