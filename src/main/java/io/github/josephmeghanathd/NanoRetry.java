package io.github.josephmeghanathd;

import io.github.josephmeghanathd.backoff.Backoff;
import io.github.josephmeghanathd.checker.CheckedRunnable;
import io.github.josephmeghanathd.checker.CheckedSupplier;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;


/**
 * NanoRetry is a lightweight, high-performance retry library designed specifically
 * for the Virtual Thread era (JDK 21+).
 * <p>
 * Unlike traditional libraries that rely on complex state machines or reactive patterns,
 * NanoRetry embraces synchronous blocking logic. When running on a Virtual Thread,
 * operations like {@code Thread.sleep()} and {@link Future#get(long, TimeUnit)}
 * unmount the virtual thread from the carrier thread, making it extremely resource-efficient.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * String result = NanoRetry.of(() -> client.call())
 *     .withMaxAttempts(3)
 *     .withBackoff(Backoff.exponential(Duration.ofMillis(100), 2.0))
 *     .retryOn(IOException.class)
 *     .withTimeout(Duration.ofSeconds(5))
 *     .execute();
 * }</pre>
 *
 * @param <T> The type of the result returned by the retryable operation.
 * @author Joseph Meghanath
 * @since 1.0.0
 */
public final class NanoRetry<T> {
    /**
     * The functional logic to be executed and potentially retried.
     */
    private final CheckedSupplier<T> action;
    /**
     * A set of exception classes that should trigger a retry.
     * If empty, the engine assumes all {@link Throwable} types are retryable.
     */
    private final Set<Class<? extends Throwable>> retryableExceptions = new HashSet<>();
    /**
     * The maximum number of attempts to perform.
     * Defaults to 1 (the initial call only).
     */
    private int maxAttempts = 1;
    /**
     * The strategy used to calculate the sleep duration between retry attempts.
     * Defaults to {@link Backoff#fixed(Duration)} with a zero-duration delay.
     */
    private Backoff backoff = Backoff.fixed(Duration.ZERO);
    /**
     * The total time limit allowed for the entire execution sequence,
     * including all retries and backoff delays.
     */
    private Duration globalTimeout = Duration.ofDays(365);

    /**
     * The maximum duration permitted for a single execution attempt.
     * If an attempt exceeds this, a {@link java.util.concurrent.TimeoutException} is thrown.
     */
    private Duration perAttemptTimeout = Duration.ofDays(365);

    /**
     * Private constructor used by static factory methods to initialize the core retry logic.
     * <p>
     * By keeping the constructor private, we enforce the use of the fluent entry points
     * {@link #of(CheckedSupplier)} and {@link #of(CheckedRunnable)}, ensuring that
     * every {@code NanoRetry} instance is initialized with a valid action.
     * </p>
     *
     * @param action The {@link CheckedSupplier} representing the primary logic to be executed.
     */
    private NanoRetry(final CheckedSupplier<T> action) {
        this.action = action;
    }

    /**
     * Initializes a retry configuration for an operation that returns a value.
     *
     * @param action A {@link CheckedSupplier} containing the logic to be retried.
     * @param <T>    The return type.
     * @return A new NanoRetry instance for fluent configuration.
     */
    public static <T> NanoRetry<T> of(final CheckedSupplier<T> action) {
        return new NanoRetry<>(action);
    }

    /**
     * Initializes a retry configuration for a side-effect operation (no return value).
     *
     * @param action A {@link CheckedRunnable} containing the logic to be retried.
     * @return A new NanoRetry instance for fluent configuration.
     */
    public static NanoRetry<Void> of(final CheckedRunnable action) {
        return new NanoRetry<>(() -> {
            action.run();
            return null;
        });
    }

    /**
     * Sets the maximum number of attempts (including the initial call).
     *
     * @param maxAttempts Total number of attempts. Default is 1.
     * @return This instance for fluent chaining.
     */
    public NanoRetry<T> withMaxAttempts(final int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    /**
     * Configures the backoff strategy to determine the delay between retries.
     *
     * @param backoff A {@link Backoff} implementation (e.g., fixed, linear, exponential).
     * @return This instance for fluent chaining.
     */
    public NanoRetry<T> withBackoff(final Backoff backoff) {
        this.backoff = backoff;
        return this;
    }

    /**
     * Specifies an exception type that should trigger a retry. If this method is never
     * called, the engine retries on all exceptions by default.
     *
     * @param exceptionClass The exception class to filter for.
     * @return This instance for fluent chaining.
     */
    public NanoRetry<T> retryOn(final Class<? extends Throwable> exceptionClass) {
        this.retryableExceptions.add(exceptionClass);
        return this;
    }

    /**
     * Sets a global timeout for the entire execution sequence (including all retries and backoffs).
     *
     * @param timeout The total allowed duration.
     * @return This instance for fluent chaining.
     */
    public NanoRetry<T> withTimeout(final Duration timeout) {
        this.globalTimeout = timeout;
        return this;
    }

    /**
     * Sets a timeout for each individual attempt. If an attempt exceeds this duration,
     * it is cancelled and treated as a failure.
     *
     * @param timeout The duration allowed per attempt.
     * @return This instance for fluent chaining.
     */
    public NanoRetry<T> withPerAttemptTimeout(final Duration timeout) {
        this.perAttemptTimeout = timeout;
        return this;
    }

    /**
     * Executes the configured retry logic.
     * <p>
     * This method uses a {@link Executors#newVirtualThreadPerTaskExecutor()} to manage timeouts.
     * The calling thread will block until a result is produced or the retry
     * policy is exhausted.
     * </p>
     *
     * @return The result of the successful operation.
     * @throws Exception The last encountered exception if all attempts fail, or a
     *                   {@link TimeoutException} if the global timeout is exceeded.
     */
    public T execute() throws Exception {
        final Instant startTime = Instant.now();
        int attempt = 0;
        Throwable lastException = null;

        // Virtual Thread Executor is used to handle per-attempt timeouts efficiently.
        // It is lightweight and follows the AutoCloseable pattern.
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            while (attempt < maxAttempts) {
                attempt++;

                // 1. Check Global Timeout
                if (Duration.between(startTime, Instant.now()).compareTo(globalTimeout) > 0) {
                    throw new TimeoutException("NanoRetry: Global timeout exceeded after " + attempt + " attempts");
                }

                try {
                    // 2. Submit the action to a Virtual Thread to enforce per-attempt timeout
                    Future<T> future = executor.submit(action::get);
                    return future.get(perAttemptTimeout.toNanos(), TimeUnit.NANOSECONDS);

                } catch (ExecutionException e) {
                    lastException = e.getCause();
                } catch (TimeoutException | InterruptedException e) {
                    lastException = e;
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                }

                // 3. Verify if we should proceed with another retry
                if (attempt < maxAttempts && shouldRetry(lastException, attempt)) {
                    // 4. Backoff delay: Parks the virtual thread, releasing the carrier thread.
                    Thread.sleep(backoff.nextDelay(attempt));
                } else {
                    break;
                }
            }
        }

        throw wrapAndThrow(lastException);
    }

    /**
     * Determines whether the execution should be retried based on the current attempt
     * count and the type of exception encountered.
     * <p>
     * The decision logic follows these rules:
     * <ol>
     *     <li>If the current attempt number has reached the maximum allowed, retry is denied.</li>
     *     <li>If no specific exceptions were registered via {@code retryOn()}, all exceptions
     *         trigger a retry.</li>
     *     <li>If specific exceptions were registered, the current exception must be an
     *         instance of at least one of them.</li>
     * </ol>
     * </p>
     *
     * @param e       The {@link Throwable} encountered during the last execution attempt.
     * @param attempt The current attempt sequence number (1-based).
     * @return {@code true} if a retry should be initiated; {@code false} otherwise.
     */
    private boolean shouldRetry(final Throwable e, final int attempt) {
        if (attempt >= maxAttempts) {
            return false;
        }
        if (retryableExceptions.isEmpty()) {
            return true;
        }
        return retryableExceptions.stream().anyMatch(clazz -> clazz.isInstance(e));
    }

    /**
     * Normalizes a {@link Throwable} into an {@link Exception} to satisfy the
     * {@code execute()} method's contract.
     * <p>
     * If the provided throwable is already an instance of {@link Exception} (checked
     * or unchecked), it is returned as is. If the throwable is a {@link java.lang.Error}
     * or another non-Exception type, it is wrapped in a {@link RuntimeException}
     * to ensure safe propagation.
     * </p>
     *
     * @param t The {@link Throwable} to be evaluated or wrapped.
     * @return An {@link Exception} ready to be thrown.
     */
    private Exception wrapAndThrow(final Throwable t) {
        if (t instanceof Exception ex) {
            return ex;
        }
        return new RuntimeException(t);
    }
}