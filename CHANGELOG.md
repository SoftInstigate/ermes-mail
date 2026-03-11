# Changelog

All notable changes to ErmesMail are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) conventions.

---

## [2.1.0] — 2025-10-14

**Full diff**: https://github.com/SoftInstigate/ermes-mail/compare/2.0.0...2.1.0

### Summary

Maintenance and security hardening release. No breaking API changes. The main theme is **secure logging**: sensitive data is now redacted from log output by default. Integration tests also gain the ability to select the SMTP security mode (SSL or STARTTLS) via environment variables, making CI pipelines easier to configure for diverse SMTP providers.

### Security

- **Secure string representations for `EmailModel` and `SMTPConfig`** (`feat: implement secure string representations for EmailModel and SMTPConfig to enhance logging security`):
  - `SMTPConfig.toString()` now redacts the `username` field — it prints `[REDACTED]` when a non-empty username is present rather than the actual value.
  - New `SMTPConfig.toSecureString()` helper omits credentials entirely and instead reports `hasCredentials=true/false`, the hostname, port, security mode and SSL port.
  - `EmailModel.toString()` now redacts the message body — it prints `message='[REDACTED]'` instead of the raw HTML/text content.
  - New `EmailModel.toSecureString()` helper omits sensitive content and reports metadata only: subject length, recipient counts (to/cc/bcc) and attachment count.
  - `EmailService` now calls `smtpConfig.toSecureString()` when logging the initialization line.
  - `SendEmailTask` now calls `model.toSecureString()` when logging the processing line.
  - Callers that need the full content for debugging can still call `toString()` explicitly, but the default log output no longer exposes email bodies or SMTP credentials.

### Features

- **Environment-variable-driven SMTP security mode selection in integration tests** (`feat: enhance SMTP configuration to support STARTTLS and SSL modes based on environment variables`):
  - The `external-smtps-conditional` scenario in `IntegrationScenariosIT` now inspects the environment variable `SMTP_INTEGRATION_STARTTLS` (also readable from `smtp-integration.properties`) to decide whether to use STARTTLS or implicit SSL (SMTPS).
  - A secondary variable `SMTP_INTEGRATION_STARTTLS_REQUIRED` controls whether STARTTLS must be enforced (maps to `SMTPConfig.forStartTlsRequired`) or is opportunistic (maps to `SMTPConfig.forStartTlsOptional`).
  - When neither variable is set, a **port heuristic** is applied: port 587 triggers STARTTLS, any other port falls back to implicit SSL. This preserves backward compatibility for existing `smtp-integration.properties` files.
  - Each branch asserts the correct `SMTPConfig.SecurityMode` to prevent accidental misconfiguration from silently downgrading security.

### Bug Fixes

- **ByteBuddy version updated to 1.17.8** (`fix: update ByteBuddy version to 1.17.8`):
  - The `bytebuddy.version` property in `pom.xml` is updated from the prior version to `1.17.8`.
  - This keeps the ByteBuddy agent (used by Mockito's inline mock-maker) in sync with the Mockito version in use and avoids potential incompatibility warnings at test startup.

### Documentation

- **`com.sun.mail:javax.mail` runtime dependency version updated to 1.6.2** (`Update javax.mail version to 1.6.2`):
  - The recommended runtime dependency snippet in `README.md` now references `javax.mail-api:1.6.2` and `com.sun.mail:javax.mail:1.6.2` (previously `1.5.6`). Note: these are the `com.sun.mail` artifacts using the pre-Jakarta `javax.mail` namespace, which is what Apache Commons Email 1.6.0 requires at runtime.
  - Version 1.6.2 is the minimum required to avoid the `NoSuchMethodError: LineOutputStream.<init>` runtime exception on classpaths that do not already supply a compatible JavaMail implementation.

- **`commons-email` version reference corrected in README** (`Update warning for ErmesMail dependency version`):
  - The warning paragraph now correctly states that ErmesMail depends on `org.apache.commons:commons-email` **v1.6** (previously stated v1.5).

- **GitHub Copilot instructions added** (`docs: add GitHub Copilot instructions for ErmesMail project`):
  - A `.github/copilot-instructions.md` file was added to describe project architecture, key development workflows, dependency-management patterns, and common pitfalls for AI-assisted development.

---

## [2.0.0] — 2025-09-08

**Full diff**: https://github.com/SoftInstigate/ermes-mail/compare/1.1.0...2.0.0

### Summary

Major release with **breaking API changes**. Introduces explicit SMTP security mode factory methods, full STARTTLS support, a testability-oriented `HtmlEmailFactory` abstraction, consolidated integration tests, and an updated CI pipeline.

### ⚠ Breaking changes

- The boolean-heavy `SMTPConfig` constructors (e.g. `new SMTPConfig("host", 25, "user", "pass", false)`) are **removed**. Callers must migrate to the factory methods listed below. Because the public API changed, all downstream code must be recompiled against 2.0.0.

### Migration guide

Replace old constructor calls with the appropriate factory method:

```java
// 2.0+ factory methods
SMTPConfig.forPlain("host", 25, "user", "pass");
SMTPConfig.forSsl("smtp.example.com", 465, "user", "pass", 465);
SMTPConfig.forStartTlsOptional("smtp.example.com", 587, "user", "pass");
SMTPConfig.forStartTlsRequired("smtp.example.com", 587, "user", "pass");
```

### Features

- **STARTTLS support** — new `SMTPConfig.SecurityMode` enum (`PLAIN`, `SSL`, `STARTTLS_OPTIONAL`, `STARTTLS_REQUIRED`) and corresponding factory methods make the transport security intent explicit and mutually exclusive.
- **`--starttls` and `--starttls-required` CLI flags** — new command-line options added to `Main`; `--sslon` is mutually exclusive with the STARTTLS flags.
- **`HtmlEmailFactory` abstraction** — a new interface and `DefaultHtmlEmailFactory` implementation allow `SendEmailTask` to be tested without a live SMTP server via Mockito.
- **Consolidated integration tests** — `IntegrationScenariosIT` replaces the older individual IT files and supports two conditional scenarios: `local-plain-mailpit` (auto-skipped when Mailpit is not running) and `external-smtps-conditional` (skipped when credentials are absent).
- **JavaMail debug mode** — integration tests set `mail.debug=true` to capture TLS handshake evidence in CI logs.

### Build & CI

- `pom.xml`: Mockito and ByteBuddy agent dependency added; `maven-surefire-plugin` and `maven-failsafe-plugin` configured with `-javaagent` argLine for inline mock-maker support.
- `.github/workflows/ci.yml` added: downloads the ByteBuddy agent JAR and passes it via `argLine` to Maven test runs; includes a `tags-ignore` filter for cleaner push triggers.
- Bitbucket pipeline configuration removed.

### Documentation

- `README.md` updated with CLI flag descriptions, migration note, and integration test instructions.
- `smtp-integration.properties.example` added (shows all supported configuration keys; the real file is git-ignored).

---

## [1.1.0] — 2022-05-25

**Full diff**: https://github.com/SoftInstigate/ermes-mail/compare/1.0.4...1.1.0

Initial public release under the `com.softinstigate` Maven group ID. Available via [JitPack](https://jitpack.io/#SoftInstigate/ermes-mail).

---

[2.1.0]: https://github.com/SoftInstigate/ermes-mail/compare/2.0.0...2.1.0
[2.0.0]: https://github.com/SoftInstigate/ermes-mail/compare/1.1.0...2.0.0
[1.1.0]: https://github.com/SoftInstigate/ermes-mail/releases/tag/1.1.0
