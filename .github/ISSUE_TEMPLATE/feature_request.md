---
name: "🚀 Feature request"
about: Suggest an idea or a new retry capability for NanoRetry
title: '[FEAT] '
labels: enhancement
assignees: ''

---

## 💡 Problem Statement
Is your feature request related to a problem? Please describe. 
*(e.g., "I am currently unable to add Jitter to my exponential backoff to prevent thundering herd problems.")*

## 🌟 Proposed Solution
A clear and concise description of what you want to happen.

## 🛠 Proposed API / Usage Syntax
How would you like to use this feature? Please provide a code snippet of the ideal fluent API call.

```java
String result = NanoRetry.of(() -> service.call())
    .withMaxAttempts(5)
    // Example of a potential new feature (e.g., event listeners):
    .onRetry((attempt, exception) -> log.warn("Retry #{} due to {}", attempt, exception.getMessage()))
    // Or a new backoff strategy:
    .withBackoff(Backoff.exponentialWithJitter(Duration.ofMillis(100), 2.0, 0.5))
    .execute();
```

## 🎯 Use Case / Rationale
Why is this feature important to you or other developers? How does it improve the project? (e.g., "This allows better observability in production logs without wrapping the execute call in a manual try-catch.")

## 🔄 Alternatives Considered
A clear and concise description of any alternative solutions or features you've considered (e.g., "I tried implementing a custom Backoff class, but it would be cleaner as a built-in static factory method.")

## 🕵️ Additional Context
Add any other context, library comparisons (e.g., how Resilience4j or Failsafe handles this), or technical constraints related to Virtual Threads/JDK 21.
```

### Why this is effective for NanoRetry:
*   **Focus on Fluent API:** It encourages users to think about how the feature fits into the existing builder pattern.
*   **Loom Awareness:** It prompts users to consider JDK 21 constraints, ensuring the library stays "Nano" and doesn't accidentally introduce heavy dependencies or reactive patterns.
*   **Observability:** Since retry logic often happens "in the dark," the example snippet specifically highlights hooks/listeners, which is a common growth area for such libraries.Here is the tailored **Feature Request** template for **NanoRetry**, designed to capture ideas for new backoff strategies, observability hooks, or integration patterns while maintaining the library's "Loom-native" philosophy.

Save this as `.github/ISSUE_TEMPLATE/feature_request.md`.

```markdown
---
name: "🚀 Feature request"
about: Suggest an idea or a new retry capability for NanoRetry
title: '[FEAT] '
labels: enhancement
assignees: ''

---

## 💡 Problem Statement
Is your feature request related to a problem? Please describe. 
*(e.g., "I am currently unable to add Jitter to my exponential backoff to prevent thundering herd problems.")*

## 🌟 Proposed Solution
A clear and concise description of what you want to happen.

## 🛠 Proposed API / Usage Syntax
How would you like to use this feature? Please provide a code snippet of the ideal fluent API call.

```java
String result = NanoRetry.of(() -> service.call())
    .withMaxAttempts(5)
    // Example of a potential new feature (e.g., event listeners):
    .onRetry((attempt, exception) -> log.warn("Retry #{} due to {}", attempt, exception.getMessage()))
    // Or a new backoff strategy:
    .withBackoff(Backoff.exponentialWithJitter(Duration.ofMillis(100), 2.0, 0.5))
    .execute();
```

## 🎯 Use Case / Rationale
Why is this feature important to you or other developers? How does it improve the project? (e.g., "This allows better observability in production logs without wrapping the execute call in a manual try-catch.")

## 🔄 Alternatives Considered
A clear and concise description of any alternative solutions or features you've considered (e.g., "I tried implementing a custom Backoff class, but it would be cleaner as a built-in static factory method.")

## 🕵️ Additional Context
Add any other context, library comparisons (e.g., how Resilience4j or Failsafe handles this), or technical constraints related to Virtual Threads/JDK 21.
