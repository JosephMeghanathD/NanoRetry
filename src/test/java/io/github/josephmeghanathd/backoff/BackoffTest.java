package io.github.josephmeghanathd.backoff;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class BackoffTest {

    @Test
    @DisplayName("Verify Fixed Backoff calculation")
    void testFixedBackoff() {
        Backoff fixed = Backoff.fixed(Duration.ofSeconds(1));
        assertEquals(1000, fixed.nextDelay(1).toMillis());
        assertEquals(1000, fixed.nextDelay(5).toMillis());
    }

    @Test
    @DisplayName("Verify Linear Backoff calculation")
    void testLinearBackoff() {
        Backoff linear = Backoff.linear(Duration.ofSeconds(1), Duration.ofSeconds(1));
        assertEquals(1000, linear.nextDelay(1).toMillis());
        assertEquals(2000, linear.nextDelay(2).toMillis());
        assertEquals(3000, linear.nextDelay(3).toMillis());
    }

    @Test
    @DisplayName("Verify Exponential Backoff calculation")
    void testExponentialBackoff() {
        Backoff expo = Backoff.exponential(Duration.ofMillis(100), 2.0);
        assertEquals(100, expo.nextDelay(1).toMillis());
        assertEquals(200, expo.nextDelay(2).toMillis());
        assertEquals(400, expo.nextDelay(3).toMillis());
    }

    @Test
    @DisplayName("Verify Random Backoff calculation and bounds")
    void testRandomBackoff() {
        Duration min = Duration.ofMillis(100);
        Duration max = Duration.ofMillis(200);
        Backoff random = Backoff.random(min, max);

        for (int i = 0; i < 100; i++) {
            long delay = random.nextDelay(1).toMillis();
            assertTrue(delay >= 100 && delay <= 200, "Delay " + delay + " out of bounds");
        }
    }

    @Test
    @DisplayName("Random Backoff should return min if min equals max")
    void testRandomBackoffEqualBounds() {
        Backoff random = Backoff.random(Duration.ofMillis(100), Duration.ofMillis(100));
        assertEquals(100, random.nextDelay(1).toMillis());
    }

    @Test
    @DisplayName("Random Backoff should throw exception if max < min")
    void testRandomBackoffInvalidBounds() {
        assertThrows(IllegalArgumentException.class, () ->
                Backoff.random(Duration.ofMillis(200), Duration.ofMillis(100))
        );
    }
}