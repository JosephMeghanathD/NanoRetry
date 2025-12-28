# ⚡ NanoRetry

**NanoRetry** is a lightweight, zero-dependency Java library designed specifically for the **Virtual Thread** era (JDK
21+). It provides a high-performance, fluent alternative to legacy retry libraries like Resilience4j, focusing on
simplicity and modern JVM concurrency primitives.

[![Java Version](https://img.shields.io/badge/Java-21%2B-orange)](https://www.oracle.com/java/technologies/javase/jdk21-relnotes.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Coverage](https://img.shields.io/badge/Coverage-100%25-brightgreen)]()

---

## 🚀 Why NanoRetry?

Traditional retry libraries were built for an era of expensive Platform Threads, relying on complex state machines,
`ScheduledExecutorServices`, or reactive patterns to avoid blocking.

**In the world of Project Loom, blocking is cheap.** NanoRetry embraces this shift by using simple, procedural logic.
When a Virtual Thread hits a backoff delay or a timeout in NanoRetry, it simply unmounts from its carrier thread,
allowing other tasks to run with near-zero overhead.

### Key Features:

* **Virtual Thread Native:** Optimized for `Thread.ofVirtual()`.
* **Zero Dependencies:** No external jars. Just pure, modern Java.
* **Checked Exception Friendly:** Use `CheckedSupplier` and `CheckedRunnable` to call I/O methods without boilerplate
  `try-catch` blocks.
* **Fluent API:** Designed for developer productivity and IDE discoverability.
* **Comprehensive Backoff:** Built-in support for Fixed, Linear, and Exponential backoff.
* **Dual-Layer Timeouts:** Configure both global execution limits and per-attempt constraints.

---

## 📦 Installation

Add the following dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>io.github.josephmeghanathd</groupId>
    <artifactId>nanoretry</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 🛠 Usage

### Basic Example

NanoRetry makes handling flaky API calls elegant:

```java
import io.github.josephmeghanathd.NanoRetry;
import io.github.josephmeghanathd.backoff.Backoff;

import java.time.Duration;

String result = NanoRetry.of(() -> apiClient.fetchData())
        .withMaxAttempts(3)
        .withBackoff(Backoff.exponential(Duration.ofMillis(100), 2.0))
        .retryOn(IOException.class)
        .withTimeout(Duration.ofSeconds(5))
        .execute();
```

### Handling Side Effects (Runnable)

If you don't need a return value, use the `Runnable` factory:

```java
NanoRetry.of(() ->database.

updateStatus(id))
        .

withMaxAttempts(5)
    .

withBackoff(Backoff.linear(Duration.ofMillis(50),Duration.

ofMillis(50)))
        .

execute();
```

### Advanced Timeout Control

NanoRetry allows you to distinguish between how long a single attempt can take and how long the entire process is
allowed to run:

```java
NanoRetry.of(() ->heavyTask.

process())
        .

withPerAttemptTimeout(Duration.ofSeconds(1)) // Max time for ONE try
        .

withTimeout(Duration.ofSeconds(10))          // Max time for ALL tries + backoffs
        .

withMaxAttempts(10)
    .

execute();
```

---

## 📐 Architecture

### Backoff Strategies

Located in the `io.github.josephmeghanathd.backoff` package:

- **Fixed:** `Backoff.fixed(Duration.ofMillis(500))`
- **Linear:** `Backoff.linear(initial, step)`
- **Exponential:** `Backoff.exponential(initial, multiplier)`

### Functional Interfaces

Located in the `io.github.josephmeghanathd.checker` package:

- **`CheckedSupplier<T>`**: Replaces `Supplier<T>`, allowing `throws Exception`.
- **`CheckedRunnable`**: Replaces `Runnable`, allowing `throws Exception`.

### Concurrency Model

NanoRetry uses `Executors.newVirtualThreadPerTaskExecutor()` internally to manage per-attempt timeouts. This ensures
that even if you call `.execute()` from a standard Platform Thread, the task monitoring is handled by lightweight
Virtual Threads.

---

## 🧪 Quality Assurance

NanoRetry is maintained with **100% code coverage**. Every branch of the retry loop, timeout logic, and backoff
calculation is verified using JUnit 5.

To run the suite:

```bash
mvn test
```

---

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Please read
our [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## 🛡️ Security

If you discover any security-related issues, please refer to our [SECURITY.md](SECURITY.md) for reporting instructions.

---

## 👤 Author

**Joseph Meghanath**

- LinkedIn: [Joseph Meghanath](https://www.linkedin.com/in/joseph-meghanath-9880ba149/)
- GitHub: [@josephmeghanathd](https://github.com/josephmeghanathd)

---

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.