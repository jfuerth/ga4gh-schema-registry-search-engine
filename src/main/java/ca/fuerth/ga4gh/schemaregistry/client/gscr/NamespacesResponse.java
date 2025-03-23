package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import java.net.URI;
import java.util.List;

public record NamespacesResponse (
    URI server, // TODO (spec) constrain this to be a URL
    List<Namespace> namespaces,
    List<Namespace> results
    ) {
    @Override
    public List<Namespace> namespaces() {
        if (namespaces != null) {
            return namespaces;
        }
        return results;
    }
}
