package io.github.josephmeghanathd.checker;

/**
 * A functional interface representing a task that can throw a checked exception.
 * <p>
 * This is a "Loom-friendly" alternative to {@link Runnable}. It allows developers
 * to pass logic into the retry engine (e.g., side-effecting operations like
 * {@code Files.delete()}) without having to wrap the logic in a boilerplate
 * {@code try-catch} block.
 * </p>
 *
 * <p>Example usage with NanoRetry:</p>
 * <pre>{@code
 * NanoRetry.of(() -> Files.delete(path)) // Files.delete throws IOException
 *          .withMaxAttempts(3)
 *          .execute();
 * }</pre>
 *
 * @author Joseph Meghanath
 * @see Runnable
 * @since 1.0.0
 */
@FunctionalInterface
public interface CheckedRunnable {

    /**
     * Executes this task.
     *
     * @throws Exception if the operation fails. Checked exceptions are permitted
     *                   to allow for seamless integration with external APIs.
     */
    void run() throws Exception;
}