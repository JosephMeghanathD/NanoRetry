package io.github.josephmeghanathd;

import io.github.josephmeghanathd.backoff.Backoff;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class NanoRetryTimeoutTest {

    @Test
    @DisplayName("Global timeout should stop execution")
    void testGlobalTimeout() {
        assertThrows(TimeoutException.class, () -> NanoRetry.of(() -> {
                    throw new IOException("Retry");
                })
                .withMaxAttempts(100000)
                .withTimeout(Duration.ofMillis(200))
                .execute());
    }

    @Test
    @DisplayName("Per-attempt timeout should trigger retry")
    void testPerAttemptTimeout() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        String result = NanoRetry.of(() -> {
                    int current = attempts.incrementAndGet();
                    if (current == 1) Thread.sleep(1000);
                    return "Recovered";
                })
                .withMaxAttempts(2)
                .withPerAttemptTimeout(Duration.ofMillis(100))
                .execute();

        assertEquals("Recovered", result);
        assertEquals(2, attempts.get());
    }

    @Test
    @DisplayName("Trigger Global Timeout Exception")
    void testGlobalTimeoutTrigger() {
        assertThrows(TimeoutException.class, () -> {
            NanoRetry.of(() -> {
                        throw new IOException("Fail");
                    })
                    .withMaxAttempts(10)
                    .withBackoff(Backoff.fixed(Duration.ofMillis(100)))
                    .withTimeout(Duration.ofMillis(50)) // Set global timeout very low
                    .execute();
        });
    }
}