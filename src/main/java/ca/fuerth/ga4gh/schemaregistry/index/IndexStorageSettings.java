package ca.fuerth.ga4gh.schemaregistry.index;

import java.time.Instant;

public record IndexStorageSettings(Instant createdAt, String embeddingModelClass, int embeddingDimensions) {
    public boolean isCompatibleWith(IndexStorageSettings other) {
        return this.embeddingModelClass.equals(other.embeddingModelClass)
                && this.embeddingDimensions == other.embeddingDimensions;
    }
}
