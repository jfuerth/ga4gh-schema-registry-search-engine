package ca.fuerth.ga4gh.schemaregistry.shared;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A carrier for a result that may have failed. If the result is a failure, the errorMessage and exception fields will be
 * populated. If the result is a success, the result field will be populated.
 *
 * @param errorMessage Error message that could be logged or presented to a user in case of failure
 * @param exception Exception that caused the failure, if any
 * @param result The result of the operation, if successful
 * @param <T> The type of the result
 */
public record FailableResult<T>(String errorMessage, Throwable exception, T result) {

    /**
     * Creates an instance representing a failure with no accompanying exception.
     *
     * @param errorMessage The error message to be logged or presented to a user
     * @return A FailableResult instance representing a failure
     * @param <T> The type of the result (just for type bookkeeping; not significant in this case)
     */
    public static <T> FailableResult<T> ofFailed(String errorMessage) {
        return new FailableResult<>(errorMessage, null, null);
    }

    /**
     * Creates an instance representing a failure with an accompanying exception.
     *
     * @param errorMessage The error message to be logged or presented to a user
     * @param exception The exception associated with the failure
     * @return A FailableResult instance representing a failure
     * @param <T> The type of the result (just for type bookkeeping; not significant in this case)
     */
    public static <T> FailableResult<T> ofFailed(String errorMessage, Throwable exception) {
        return new FailableResult<>(errorMessage, exception, null);
    }

    /**
     * Creates an instance representing a successful result.
     *
     * @param result The result of the operation
     * @return A FailableResult instance representing a success
     * @param <T> The type of the result
     */
    public static <T> FailableResult<T> ofSuccess(T result) {
        return new FailableResult<>(null, null, result);
    }

    /**
     * Creates a FailableResult instance by running the given Supplier. If the Supplier throws an exception, the
     * exception will be caught and wrapped in a FailableResult instance representing a failure.
     *
     * @param failMessage The error message to be logged or presented to a user in case of failure
     * @param resultSupplier The Supplier that will provide the result or throw an exception
     * @return A FailableResult instance representing the result of the operation
     * @param <T> The type of the result
     */
    public static <T> FailableResult<T> of(String failMessage, Supplier<T> resultSupplier) {
        try {
            return FailableResult.ofSuccess(resultSupplier.get());
        } catch (Exception e) {
            return FailableResult.ofFailed(failMessage, e);
        }
    }

    /**
     * Converts the result of this FailableResult to a new type using the given mapper function. If this FailableResult
     * represents a failure, the failure will be propagated to the new FailableResult instance.
     *
     * @param failMessage The error message to be logged or presented to a user in case the mapper fails. Does not
     *                    replace the original error message.
     * @param mapper The function that will convert the result to a new type. If the mapper throws an exception,
     *               the result will be a failure with the message provided by the failMessage function.
     * @return A FailableResult instance:
     * <ul>
     *     <li>if this result is failed, the output is a failed result with this result's failure message</li>
     *     <li>if this result is success:
     *         <ul>
     *             <li>if the mapper succeeds, a FailableResult containing the result of the mapper</li>
     *             <li>if the mapper throws, a FailableResult containing the message generated by {@code failMessage}
     *             plus the exception thrown by the mapper</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param <O> The type of the downstream result
     */
    @NotNull
    public <O> FailableResult<O> map(Function<T, String> failMessage, Function<T, O> mapper) {
        if (isFailed()) {
            return new FailableResult<>(errorMessage, exception, null);
        }
        return FailableResult.of(failMessage.apply(result), () -> mapper.apply(result));
    }

    /**
     * Expands the contents of this FailableResult into a stream of FailiableResults wr
     * @param failMessage a function that generates a failure message in case the flattening process throws an exception
     * @param flattener the function that converts this container's result into a stream of 0 or more items
     * @return A stream of FailableResult instances:
     * <ul>
     *     <li>if this result is failed, a single failed result with this result's failure message</li>
     *     <li>if this result is success:
     *         <ul>
     *             <li>if the flattener succeeds, a stream of successful FailableResults, each containing an element of the flattener's stream</li>
     *             <li>if the flattener throws, a single failed FailableResult containing the message generated by {@code failMessage}
     *             plus the exception thrown by the flattener</li>
     *         </ul>
     *     </li>
     * </ul>
     * @param <O> The type of the downstream results
     */
    @NotNull
    public <O> Stream<FailableResult<O>> flatten(Function<T, String> failMessage, Function<T, Stream<O>> flattener) {
        if (isFailed()) {
            return Stream.of(new FailableResult<>(errorMessage, exception, null));
        }
        try {
            return flattener.apply(result).map(FailableResult::ofSuccess);
        } catch (Exception e) {
            return Stream.of(FailableResult.ofFailed(failMessage.apply(result), e));
        }
    }

    public boolean isFailed() {
        return result == null;
    }

    public boolean isSuccess() {
        return result != null;
    }
}
