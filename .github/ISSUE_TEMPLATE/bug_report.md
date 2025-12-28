---
name: 🐛 Bug report
about: Create a report to help us improve NanoRetry
title: '[BUG] '
labels: bug
assignees: ''

---

## 📝 Description
A clear and concise description of what the bug is. Is it related to retry counts, backoff timing, or Virtual Thread behavior?

## 🔄 Steps to Reproduce
1. Configure `NanoRetry` with specific parameters (attempts, backoff, timeouts)...
2. Execute the retry logic within a specific environment (e.g., inside/outside a Virtual Thread)...
3. Observe the unexpected behavior (e.g., incorrect number of retries, timeout not triggering).

## 💻 Sample Code
Please provide a minimal code snippet to help us reproduce the issue.

```java
import io.github.josephmeghanathd.NanoRetry;
import io.github.josephmeghanathd.backoff.Backoff;
import java.time.Duration;

// Example of the failing configuration
public void reproduce() throws Exception {
    String result = NanoRetry.of(() -> {
            // Logic that causes the issue
            throw new IOException("Temporary failure");
        })
        .withMaxAttempts(3)
        .withBackoff(Backoff.fixed(Duration.ofMillis(100)))
        .retryOn(IOException.class)
        .execute();
}
```

## 🎯 Expected Behavior
A clear and concise description of what you expected to happen (e.g., "The operation should have retried exactly 3 times before throwing the exception").

## ❌ Actual Behavior
What actually happened? (e.g., "It only attempted once," "The thread stayed blocked indefinitely," or "It threw a NullPointerException instead of an IOException").

## ⚙️ Environment
*   **NanoRetry Version:** [e.g., 1.0.0]
*   **Java Version:** [e.g., Java 21] (Note: NanoRetry requires JDK 21+ for Virtual Threads)
*   **OS:** [e.g., Linux, macOS, Windows]
*   **Context:** [e.g., Running inside a Virtual Thread, Spring Boot 3.2, etc.]

## 📸 Screenshots
If applicable, add screenshots or logs (especially thread dumps if dealing with deadlocks or Virtual Thread pinning).

## 🕵️ Additional Context
Add any other context about the problem here (e.g., specific `Backoff` strategy used, or if the issue only occurs under high concurrency).