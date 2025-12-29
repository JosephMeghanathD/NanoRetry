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

    @Test
    @DisplayName("exponentialWithJitter: Verify bounds [0, exponentialMax]")
    void testJitterBounds() {
        Duration initial = Duration.ofMillis(100);
        double multiplier = 2.0;
        Backoff jitterBackoff = Backoff.exponentialWithJitter(initial, multiplier);

        // Attempt 1: expDelay = 100 * 2^0 = 100. Jitter range [0, 100]
        // Attempt 3: expDelay = 100 * 2^2 = 400. Jitter range [0, 400]

        for (int i = 0; i < 1000; i++) {
            long delay1 = jitterBackoff.nextDelay(1).toMillis();
            long delay3 = jitterBackoff.nextDelay(3).toMillis();

            assertTrue(delay1 >= 0 && delay1 <= 100, "Attempt 1 out of bounds: " + delay1);
            assertTrue(delay3 >= 0 && delay3 <= 400, "Attempt 3 out of bounds: " + delay3);
        }
    }

    @Test
    @DisplayName("exponentialWithJitter: Verify entropy (not returning constant values)")
    void testJitterEntropy() {
        Backoff jitterBackoff = Backoff.exponentialWithJitter(Duration.ofMillis(1000), 2.0);

        long first = jitterBackoff.nextDelay(5).toMillis();
        boolean foundDifferent = false;

        // Statistically, over 100 tries with a large range [0, 16000],
        // we should definitely see a different number.
        for (int i = 0; i < 100; i++) {
            if (jitterBackoff.nextDelay(5).toMillis() != first) {
                foundDifferent = true;
                break;
            }
        }
        assertTrue(foundDifferent, "Jitter should produce varying values");
    }

    @Test
    @DisplayName("exponentialWithJitter: Handle very small initial delay")
    void testJitterSmallInitial() {
        Backoff jitterBackoff = Backoff.exponentialWithJitter(Duration.ofMillis(1), 2.0);
        // expDelay = 1. Range [0, 1]. Should not throw exception.
        long delay = jitterBackoff.nextDelay(1).toMillis();
        assertTrue(delay == 0 || delay == 1);
    }

    @Test
    @DisplayName("exponentialWithJitter: Handle zero initial delay")
    void testJitterZeroInitial() {
        Backoff jitterBackoff = Backoff.exponentialWithJitter(Duration.ZERO, 2.0);
        // range [0, 0]. Must always be 0.
        assertEquals(0, jitterBackoff.nextDelay(5).toMillis());
    }
}