# Changelog

All notable changes to ErmesMail are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) conventions.

---

## [3.0.0] — 2026-08-02

**Full diff**: https://github.com/SoftInstigate/ermes-mail/compare/2.1.0...3.0.0

### Summary

Major release with **breaking API changes** and significant modernization. Recipient and Attachment classes converted to Java records, input validation hardened across all constructors, and EmailService now supports lazy thread pool initialization with virtual-threads-friendly `poolSize=0` mode. Comprehensive test suite added (74 tests).

### ⚠ Breaking changes

- **`Recipient` and `Attachment` are now Java records** — direct field access (`recipient.email`) must change to accessor methods (`recipient.email()`). Same for `attachment.url()`, `attachment.fileName()`, `attachment.description()`.
- **Input validation on constructors** — `null` values for `from`, `subject`, `message` in `EmailModel`, and `email` in `Recipient`, `url`/`fileName` in `Attachment`, now throw `NullPointerException` immediately at construction time instead of failing later during SMTP send.
- **`EmailModel.toString()`** changed from `"MailModel{"` to `"EmailModel{"` (consistent with class name).
- **`EmailModel.toSecureString()`** changed from `"MailModel{"` to `"EmailModel{"`.
- **`EmailService.send()` after shutdown** throws `IllegalStateException` instead of `RejectedExecutionException`.
- **`SMTPConfig` validation** — null/blank hostname and port outside 1–65535 now throw `IllegalArgumentException` in factory methods.

### Migration guide

Update field accesses to record accessor methods:

```java
// Before (2.x)
recipient.email
recipient.name
attachment.url
attachment.fileName

// After (3.0)
recipient.email()
recipient.name()
attachment.url()
attachment.fileName()
```

### Features

- **Lazy thread pool** — the `ExecutorService` in `EmailService` is created lazily on the first `send()` call, not at construction time. Applications that only use `sendSynch()` never create a thread pool.
- **`threadPoolSize = 0`** — no internal pool is created; `send()` falls back to synchronous execution, returning an already-completed `Future`. Useful when the caller manages concurrency externally (e.g. virtual threads in RestHeart).
- **`AutoCloseable`** — `EmailService` implements `AutoCloseable` for use with try-with-resources.
- **Socket timeouts** — `SMTPConfig` now includes configurable `connectionTimeout` (default 10s) and `socketTimeout` (default 60s), applied via `HtmlEmail.setSocketConnectionTimeout()` and `HtmlEmail.setSocketTimeout()`.
- **Default constructor** — `new EmailService(smtpConfig)` uses `Runtime.getRuntime().availableProcessors()` as pool size (lazy).
- **Input validation** — early `NullPointerException` / `IllegalArgumentException` on invalid inputs across `SMTPConfig`, `EmailModel`, `Recipient`, and `Attachment`.

### Error handling improvements

- **`processAttachments()` catches `IllegalArgumentException`** — `URI.create()` with malformed URIs no longer escapes as an uncaught exception; the error is added to the error list.
- **`SendEmailTask.call()` catches `RuntimeException`** — unexpected unchecked exceptions are captured in the error list instead of escaping the task.
- **`EmailService.shutdown()` forces `shutdownNow()`** — after the await timeout, abandoned tasks are now properly logged and terminated.
- **`Main.call()` logging** — replaced `e.printStackTrace()` with proper `LOGGER.log(Level.SEVERE, ...)`; `InterruptedException` now restores the interrupt flag; `shutdown()` moved to `try-finally`.

### Java 17 modernization

- **Records** — `Recipient` and `Attachment` converted from static inner classes to records with compact constructor validation.
- **`List.copyOf()`** — replaces `Collections.unmodifiableList()` for true immutable snapshots.
- **Switch expression** — `SecurityMode` handling in `SendEmailTask` uses enhanced switch with `→` syntax.
- **`StandardCharsets.UTF_8.name()`** — replaces `"UTF-8"` magic string.
- **`MailcapCommandMap`** — initialized once via `AtomicBoolean` instead of on every `send()` call.

### Documentation

- **Javadoc** updated across all classes: `@param`/`@return` on all public methods, field documentation, record component descriptions, private method documentation.
- **README.md** rewritten: accurate API examples, virtual threads section, socket timeouts section, 2.x→3.0 migration guide.

### Testing

- **74 unit tests** covering all production classes:
  - `EmailModelTest` (29 tests) — constructor, add/set recipients, unmodifiable lists, toString, toSecureString, validation
  - `SMTPConfigTest` (11 tests) — factory methods, field assertions, toString redaction, toSecureString
  - `SendEmailTaskTest` (12 tests) — all security modes, error paths, attachments, CC/BCC
  - `EmailServiceTest` (15 tests) — sync/async, poolSize=0, lazy executor, shutdown, validation
  - `MainCliTest` (4 tests) — CLI parsing, mutual exclusion runtime check
  - `DefaultHtmlEmailFactoryTest` (3 tests) — factory contract

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

[3.0.0]: https://github.com/SoftInstigate/ermes-mail/compare/2.1.0...3.0.0
[2.1.0]: https://github.com/SoftInstigate/ermes-mail/compare/2.0.0...2.1.0
[2.0.0]: https://github.com/SoftInstigate/ermes-mail/compare/1.1.0...2.0.0
[1.1.0]: https://github.com/SoftInstigate/ermes-mail/releases/tag/1.1.0
