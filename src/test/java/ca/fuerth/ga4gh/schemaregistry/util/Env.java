package ca.fuerth.ga4gh.schemaregistry.util;

import java.util.Objects;

public class Env {
    public static String requiredEnv(String name) {
        return Objects.requireNonNull(System.getenv(name), "Missing required environment variable " + name);
    }
}
