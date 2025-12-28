package io.github.josephmeghanathd.backoff;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Strategy interface for calculating the wait duration between retry attempts.
 * <p>
 * This interface is designed for use with Virtual Threads, where blocking via
 * {@link Thread#sleep(Duration)} is efficient and preferred.
 * </p>
 *
 * @author Joseph Meghanath
 * @since 1.0.0
 */
public interface Backoff {

    /**
     * Creates a backoff strategy that uses a constant delay between every attempt.
     *
     * @param delay The fixed duration to wait after every failure.
     * @return A fixed-interval {@link Backoff} strategy.
     */
    static Backoff fixed(Duration delay) {
        return attempt -> delay;
    }

    /**
     * Creates a backoff strategy that increases the delay linearly.
     * <p>
     * Formula: {@code initialDelay + (step * (attempt - 1))}
     * </p>
     *
     * @param initialDelay The delay applied after the first failure.
     * @param step         The duration added to the delay for each subsequent attempt.
     * @return A linear {@link Backoff} strategy.
     */
    static Backoff linear(Duration initialDelay, Duration step) {
        return attempt -> initialDelay.plus(step.multipliedBy(attempt - 1));
    }

    /**
     * Creates a backoff strategy that increases the delay exponentially.
     * <p>
     * Formula: {@code initialDelay * (multiplier ^ (attempt - 1))}
     * </p>
     *
     * @param initialDelay The base delay for the first retry attempt.
     * @param multiplier   The growth factor (e.g., 2.0 to double the delay each time).
     * @return An exponential {@link Backoff} strategy.
     */
    static Backoff exponential(Duration initialDelay, double multiplier) {
        return attempt -> Duration.ofMillis((long) (initialDelay.toMillis() * Math.pow(multiplier, attempt - 1)));
    }

    /**
     * Creates a backoff strategy that picks a random delay within a specified range.
     * <p>
     * This is useful for avoiding "thundering herd" problems by adding entropy
     * to the retry timing.
     * </p>
     *
     * @param minDelay The minimum inclusive delay.
     * @param maxDelay The maximum inclusive delay.
     * @return A random {@link Backoff} strategy.
     * @throws IllegalArgumentException if maxDelay is less than minDelay.
     */
    static Backoff random(final Duration minDelay, final Duration maxDelay) {
        final long min = minDelay.toMillis();
        final long max = maxDelay.toMillis();
        if (max < min) {
            throw new IllegalArgumentException("maxDelay must be greater than or equal to minDelay");
        }
        return attempt -> {
            if (max == min) return minDelay;
            return Duration.ofMillis(ThreadLocalRandom.current().nextLong(min, max + 1));
        };
    }

    /**
     * Calculates the duration to wait before the next retry attempt.
     *
     * @param attempt The current attempt number (1-based index).
     * @return The {@link Duration} to wait.
     */
    Duration nextDelay(int attempt);
}