package ca.fuerth.ga4gh.schemaregistry.api;

import java.util.Map;

public record SearchHit(Map<String, Object> metadata, String text) {
}
