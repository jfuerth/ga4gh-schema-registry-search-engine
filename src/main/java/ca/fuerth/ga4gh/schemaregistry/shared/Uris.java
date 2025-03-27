package ca.fuerth.ga4gh.schemaregistry.shared;

import java.net.URI;

public class Uris {

    public static URI normalizeUri(URI uri) {
        String asString = uri.toString().trim();
        return URI.create(stripTrailingSlash(asString)).normalize();
    }

    public static String stripTrailingSlash(String uri) {
        if (uri.endsWith("/")) {
            return uri.substring(0, uri.length() - 1);
        }
        return uri;
    }
}
