package ca.fuerth.ga4gh.schemaregistry.crawler;

import ca.fuerth.ga4gh.schemaregistry.client.gscr.Namespace;
import ca.fuerth.ga4gh.schemaregistry.client.gscr.SchemaRecord;

import java.net.URI;

public record CrawledSchema(
        URI registryBaseUri,
        Namespace namespace,
        SchemaRecord schemaRecord,
        URI schemaVersionUri,
//        SchemaVersion schemaVersionInfo, TODO enable when there's a server implementation that does versions
        String jsonSchema
) {
}
