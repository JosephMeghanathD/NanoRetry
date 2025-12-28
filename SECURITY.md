# Security Policy

## Supported Versions

We take the security of NanoRetry seriously. As a modern, lightweight library, we focus our security efforts on the current stable releases.

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | ✅ Yes             |
| < 1.0.0 | ❌ No              |

## Reporting a Vulnerability

**Please do not report security vulnerabilities through public GitHub issues.**

If you discover a potential security vulnerability in this project (e.g., resource leaks, thread starvation, or improper exception handling that could lead to data exposure), please notify us privately so we can address it before it is made public.

### How to Report
You can report security concerns through either of the following channels:

1.  **Email:** Send a detailed report to **josephdanthikolla@gmail.com**.
2.  **LinkedIn:** Reach out directly to [Joseph Meghanath](https://www.linkedin.com/in/joseph-meghanath-9880ba149/).

### What to Include
To help us triage and fix the issue quickly, please include:
*   A clear description of the vulnerability.
*   Steps to reproduce the issue (a minimal Java snippet using `NanoRetry` is preferred).
*   The potential impact (e.g., "This causes Virtual Thread pinning" or "This leads to unhandled TimeoutExceptions").

### Our Response Process
*   **Acknowledgment:** We will acknowledge receipt of your report within 48 hours.
*   **Investigation:** We will investigate the issue and may contact you for further details or testing.
*   **Fix & Disclosure:** Once a fix is verified, we will release a new version. We follow a "coordinated disclosure" policy and ask that you do not share information about the vulnerability publicly until a patch has been released.

## Safety First
As a zero-dependency library designed for high-concurrency environments, we appreciate the efforts of security researchers who help keep the Java ecosystem safe. Thank you for your help!

---
*Maintained by Joseph Meghanath.*