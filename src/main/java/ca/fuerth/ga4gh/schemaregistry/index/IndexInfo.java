package ca.fuerth.ga4gh.schemaregistry.index;

import java.time.Instant;

public record IndexInfo(Instant createdAt, String embeddingModelClass, int embeddingDimensions) {
    public boolean isCompatibleWith(IndexInfo other) {
        return this.embeddingModelClass.equals(other.embeddingModelClass)
                && this.embeddingDimensions == other.embeddingDimensions;
    }
}
