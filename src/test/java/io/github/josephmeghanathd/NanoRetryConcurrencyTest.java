package io.github.josephmeghanathd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class NanoRetryConcurrencyTest {

    @Test
    @DisplayName("Verify execution within a Virtual Thread")
    void testOnVirtualThread() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        Thread.ofVirtual().start(() -> {
            try {
                NanoRetry.of(() -> {
                    attempts.incrementAndGet();
                    return "Done";
                }).execute();
            } catch (Exception ignored) {}
        }).join();
        assertEquals(1, attempts.get());
    }

    @Test
    @DisplayName("Proper handling of InterruptedException during backoff")
    void testInterruption() {
        Thread.currentThread().interrupt();
        assertThrows(InterruptedException.class, () -> NanoRetry.of(() -> "work")
                .withMaxAttempts(2)
                .withBackoff(attempt -> Duration.ofMillis(500))
                .execute());
        Thread.interrupted(); // clear flag
    }
}