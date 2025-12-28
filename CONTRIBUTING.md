# Contributing to NanoRetry

First off, thank you for considering contributing to NanoRetry! This library is built for the modern Java era (Loom/JDK 21+), and it's contributors like you who help keep it lightweight, fast, and developer-friendly.

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

## How Can I Contribute?

### 🛠️ Improvements and Suggestions
Whether you have a bug fix, a new backoff strategy, or a performance optimization for Virtual Threads, all contributions are welcome. 

1.  **Fork** the repository to your own GitHub account.
2.  **Clone** your fork to your local machine.
3.  Create a new **branch** for your changes (e.g., `git checkout -b feat/jitter-backoff`).
4.  Commit your changes with clear, descriptive commit messages.
5.  **Push** your branch to your fork.
6.  Open a **Pull Request (PR)** against our `main` branch.

### 🧪 Reporting Bugs
If you find a bug, please open an issue using our Bug Report template. Include:
*   A clear description of the behavior.
*   The environment details (Are you running inside a Virtual Thread?).
*   Steps to reproduce the issue with a minimal code snippet.

## Development Setup

To work on this project locally, you will need:
*   **Java 21** or higher (Virtual Threads/Loom support is mandatory).
*   **Maven** 3.x.

### Running Tests
Before submitting a PR, please ensure all tests pass and **test coverage remains at 100%**:
```bash
mvn test
```
If you add a new strategy or feature, please add corresponding test cases in the appropriate sub-package under `src/test/java/io/github/josephmeghanathd/`.

## Project Philosophy & Coding Standards

To maintain the "Nano" in NanoRetry, please adhere to these principles:

1.  **Zero External Dependencies:** No libraries outside of the standard JDK are allowed (except for JUnit in the test scope).
2.  **Virtual Thread Native:** Logic should be simple and procedural. Do not use `ScheduledExecutorService`, `CompletableFuture` chains, or Reactive patterns. Use blocking logic (`Thread.sleep()`, `Future.get()`) as it is highly efficient on Virtual Threads.
3.  **Fluent API:** Keep the developer experience as the top priority. Methods should be easy to discover via IDE autocomplete.
4.  **Package Structure:**
    *   Core engine remains in `io.github.josephmeghanathd`.
    *   Functional interfaces belong in `.checker`.
    *   Backoff strategies belong in `.backoff`.
5.  **Final Parameters:** Use `final` for method parameters to align with the existing code style and promote immutability.

## Get in Touch

I am always open to discussing new ideas, architectural shifts, or networking with fellow Java architects.

*   **LinkedIn:** [Joseph Meghanath](https://www.linkedin.com/in/joseph-meghanath-9880ba149/)
*   **Email:** josephdanthikolla@gmail.com

---
*Happy Coding! Let's make retries simple again.* 🚀
