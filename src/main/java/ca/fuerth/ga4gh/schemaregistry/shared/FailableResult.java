package ca.fuerth.ga4gh.schemaregistry.shared;

import java.util.function.Function;
import java.util.function.Supplier;

public record FailableResult<T>(String errorMessage, Throwable exception, T result) {

    public static <T> FailableResult<T> ofFailed(String errorMessage) {
        return new FailableResult<>(errorMessage, null, null);
    }

    public static <T> FailableResult<T> ofFailed(String errorMessage, Throwable exception) {
        return new FailableResult<>(errorMessage, exception, null);
    }

    public static <T> FailableResult<T> ofSuccess(T result) {
        return new FailableResult<>(null, null, result);
    }

    public static <T> FailableResult<T> of(String failMessage, Supplier<T> resultSupplier) {
        try {
            return FailableResult.ofSuccess(resultSupplier.get());
        } catch (Exception e) {
            return FailableResult.ofFailed(failMessage, e);
        }
    }

    public <O> FailableResult<O> map(Function<T, String> failMessage, Function<T, O> mapper) {
        if (isFailed()) {
            return new FailableResult<>(errorMessage, exception, null);
        }
        return FailableResult.of(failMessage.apply(result), () -> mapper.apply(result));
    }

    public boolean isFailed() {
        return result == null;
    }

    public boolean isSuccess() {
        return result != null;
    }
}
