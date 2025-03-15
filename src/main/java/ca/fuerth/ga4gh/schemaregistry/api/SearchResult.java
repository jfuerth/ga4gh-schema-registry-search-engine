package ca.fuerth.ga4gh.schemaregistry.api;

import java.util.List;

public record SearchResult(List<SearchHit> hits) {
}
