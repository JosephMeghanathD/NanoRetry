package io.github.josephmeghanathd.checker;

/**
 * A functional interface representing a supplier of results that may throw a checked exception.
 * <p>
 * This interface is a specialized version of {@link java.util.function.Supplier}. It is designed
 * to handle logic that returns a value—such as database queries, network calls, or file reads—while
 * allowing checked exceptions (e.g., {@link java.io.IOException}, {@link java.sql.SQLException})
 * to propagate without requiring manual wrapping.
 * </p>
 *
 * <p>Example usage with NanoRetry:</p>
 * <pre>{@code
 * String data = NanoRetry.of(() -> client.fetchData()) // fetchData() throws IOException
 *                        .withMaxAttempts(3)
 *                        .execute();
 * }</pre>
 *
 * @param <T> the type of results supplied by this supplier
 * @author Joseph Meghanath
 * @see java.util.function.Supplier
 * @since 1.0.0
 */
@FunctionalInterface
public interface CheckedSupplier<T> {

    /**
     * Gets a result.
     *
     * @return the supplied result
     * @throws Exception if the operation fails. Checked exceptions are permitted
     *                   to allow for seamless integration with external APIs.
     */
    T get() throws Exception;
}