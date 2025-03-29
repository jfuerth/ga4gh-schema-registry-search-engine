package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import jakarta.validation.ConstraintViolation;

import java.util.Set;
import java.util.stream.Collectors;

public class Ga4ghSchemaRegistryProtocolException extends RuntimeException {

    public Ga4ghSchemaRegistryProtocolException(String message) {
        super(message);
    }

    public static Ga4ghSchemaRegistryProtocolException ofValidationErrors(String message, Set<ConstraintViolation<Object>> validationErrors) {
        String formattedErrors = validationErrors.stream()
                .map(cv -> cv.getPropertyPath() + " " + cv.getMessage())
                .collect(Collectors.joining( ", " ));

        return new Ga4ghSchemaRegistryProtocolException(message + ": " + formattedErrors);
    }
}
