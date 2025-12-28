package io.github.josephmeghanathd;

import io.github.josephmeghanathd.backoff.Backoff;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class NanoRetryCoreTest {

    @Test
    @DisplayName("Success on first attempt (Supplier)")
    void testSupplierSuccess() throws Exception {
        String result = NanoRetry.of(() -> "Hello").withMaxAttempts(3).execute();
        assertEquals("Hello", result);
    }

    @Test
    @DisplayName("Success on first attempt (Runnable)")
    void testRunnableSuccess() throws Exception {
        AtomicInteger count = new AtomicInteger(0);
        NanoRetry.of(count::incrementAndGet).execute();
        assertEquals(1, count.get());
    }

    @Test
    @DisplayName("Retry success after multiple failures")
    void testRetrySuccess() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        String result = NanoRetry.of(() -> {
                    if (attempts.incrementAndGet() < 3) throw new IOException("Fail");
                    return "Success";
                })
                .withMaxAttempts(5)
                .withBackoff(Backoff.fixed(Duration.ofMillis(10)))
                .retryOn(IOException.class)
                .execute();

        assertEquals("Success", result);
        assertEquals(3, attempts.get());
    }

    @Test
    @DisplayName("Fail when max attempts are exhausted")
    void testMaxAttemptsExhausted() {
        assertThrows(IOException.class, () -> NanoRetry.of(() -> {
            throw new IOException("Persistent error");
        }).withMaxAttempts(3).execute());
    }

    @Test
    @DisplayName("Should not retry on non-specified exceptions")
    void testRetryFilterMismatch() {
        AtomicInteger attempts = new AtomicInteger(0);
        assertThrows(SQLException.class, () -> NanoRetry.of(() -> {
                    attempts.incrementAndGet();
                    throw new SQLException("DB Error");
                })
                .retryOn(IOException.class)
                .execute());
        assertEquals(1, attempts.get());
    }

    @Test
    @DisplayName("Throwing RuntimeException if cause is not an Exception (e.g. Error)")
    void testThrowableWrapper() {
        assertThrows(RuntimeException.class, () -> NanoRetry.of(() -> {
            throw new Error("Fatal Error");
        }).execute());
    }

    @Test
    @DisplayName("Should hit the final throw line when all retries are exhausted")
    void testExhaustedRetriesCoverage() {
        AtomicInteger attempts = new AtomicInteger(0);

        Exception exception = assertThrows(IOException.class, () -> {
            NanoRetry.of(() -> {
                        attempts.incrementAndGet();
                        throw new IOException("Persistent failure");
                    })
                    .withMaxAttempts(3)
                    .withBackoff(Backoff.fixed(Duration.ofMillis(1)))
                    .execute();
        });

        assertEquals("Persistent failure", exception.getMessage());
        assertEquals(3, attempts.get());
    }

    @Test
    @DisplayName("Cover Final Throw on Exhaustion")
    void testExhaustionPath() {
        AtomicInteger count = new AtomicInteger(0);
        assertThrows(IOException.class, () -> {
            NanoRetry.of(() -> {
                        count.incrementAndGet();
                        throw new IOException("Persistent");
                    })
                    .withMaxAttempts(2)
                    .withBackoff(Backoff.fixed(Duration.ofMillis(1)))
                    .execute();
        });
        assertEquals(2, count.get());
    }

    @Test
    @DisplayName("Cover loop break on non-retryable exception")
    void testNonRetryableBreak() {
        assertThrows(RuntimeException.class, () -> {
            NanoRetry.of(() -> {
                        throw new RuntimeException("Fatal");
                    })
                    .withMaxAttempts(5)
                    .retryOn(IOException.class) // Only retry IO
                    .execute();
        });
    }

    @Test
    @DisplayName("Should successfully execute and retry a CheckedRunnable (side-effect)")
    void testRunnableSuccessOf() throws Exception {
        AtomicInteger count = new AtomicInteger(0);

        // This calls the of(CheckedRunnable) factory method
        NanoRetry.of(() -> {
                    if (count.incrementAndGet() < 2) {
                        throw new IOException("Side-effect failure");
                    }
                })
                .withMaxAttempts(3)
                .retryOn(IOException.class)
                .withBackoff(Backoff.fixed(Duration.ofMillis(1)))
                .execute();

        // Verify that the runnable logic was actually executed twice
        assertEquals(2, count.get());
    }

    @Test
    @DisplayName("Cover while-loop False branch via exhaustion")
    void testWhileLoopFalseBranch() {
        assertThrows(IOException.class, () -> {
            NanoRetry.of(() -> {
                        throw new IOException("Persistent");
                    })
                    .withMaxAttempts(2)
                    .withBackoff(Backoff.fixed(Duration.ofMillis(1)))
                    .execute();
        });
    }

    @Test
    @DisplayName("Cover while-loop False branch by skipping entirely")
    void testWhileLoopSkipped() {
        assertThrows(RuntimeException.class, () -> {
            NanoRetry.of(() -> "never-run")
                    .withMaxAttempts(0)
                    .execute();
        });
    }

    @Test
    @DisplayName("shouldRetry: Cover True branch of attempt check")
    void testShouldRetryMaxAttemptsReached() {
        assertThrows(IOException.class, () -> {
            NanoRetry.of(() -> {
                        throw new IOException("Final fail");
                    })
                    .withMaxAttempts(1) // Only 1 attempt allowed
                    .execute();
        });
    }

    @Test
    @DisplayName("shouldRetry: Cover True branch of empty exception list")
    void testShouldRetryAllExceptions() throws Exception {
        AtomicInteger count = new AtomicInteger(0);
        NanoRetry.of(() -> {
                    if (count.incrementAndGet() < 2) throw new RuntimeException("Generic fail");
                    return "ok";
                })
                .withMaxAttempts(2)
                .execute();

        assertEquals(2, count.get());
    }

    @Test
    @DisplayName("shouldRetry: Cover False branch of anyMatch (non-retryable)")
    void testShouldRetryMismatch() {
        assertThrows(RuntimeException.class, () -> {
            NanoRetry.of(() -> {
                        throw new RuntimeException("Not an IOException");
                    })
                    .withMaxAttempts(3)
                    .retryOn(IOException.class)
                    .execute();
        });
    }
}