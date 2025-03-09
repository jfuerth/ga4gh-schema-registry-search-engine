package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import java.net.URI;
import java.util.List;

public record NamespacesResponse (
    URI server, // TODO (spec) constrain this to be a URL
    List<Namespace> namespaces) {
}
