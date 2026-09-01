---
type: Domain
title: ErmesMail Domain Concepts
description: Core domain objects in ErmesMail — SMTPConfig, EmailModel, EmailService, SendEmailTask, HtmlEmailFactory, and Main.
tags: [domain, smtp, email, config, model]
verified:
  - by: openwiki/0.4.3
    at: 2026-09-01T09:33:22.001Z
sources:
  - id: openwiki-source-b7f22bb017d700f0525c051c
    resource: repo://src/main/java/com/softinstigate/ermes/mail/DefaultHtmlEmailFactory.java
  - id: openwiki-source-6c8b662796c793acd21608be
    resource: repo://src/main/java/com/softinstigate/ermes/mail/EmailModel.java
  - id: openwiki-source-9c9aef1c80259f898ff61a39
    resource: repo://src/main/java/com/softinstigate/ermes/mail/EmailService.java
  - id: openwiki-source-02219b7976a0c9e88905c6dd
    resource: repo://src/main/java/com/softinstigate/ermes/mail/HtmlEmailFactory.java
  - id: openwiki-source-379f409926d155ca61dee235
    resource: repo://src/main/java/com/softinstigate/ermes/mail/Main.java
  - id: openwiki-source-1f12b1ce7d6a6ee6ae4915a4
    resource: repo://src/main/java/com/softinstigate/ermes/mail/SendEmailTask.java
  - id: openwiki-source-9c21b3f210c87fd6502ff018
    resource: repo://src/main/java/com/softinstigate/ermes/mail/SMTPConfig.java
  - id: openwiki-source-c1b9dac4ccfdcefca48255c3
    resource: repo://src/test/java/com/softinstigate/ermes/mail/EmailServiceTest.java
  - id: openwiki-source-3d6eb9099e3c0ec4cef2e98c
    resource: repo://src/test/java/com/softinstigate/ermes/mail/SendEmailTaskTest.java
generated: { by: "openwiki/0.4.3", at: "2026-09-01T09:33:22.001Z" }
---

# Domain Concepts

## SMTPConfig

**Source:** `src/main/java/com/softinstigate/ermes/mail/SMTPConfig.java`
**Source Map:** See [SMTPConfig.java](../source-map.md#smtpconfigjava) for change guidance.

Holds SMTP server connection details. Construction is via static factory methods (not public constructors) to make the security intent explicit.

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `hostname` | `String` | SMTP server hostname |
| `port` | `int` | SMTP port (typically 25, 587, or 465) |
| `username` | `String` | SMTP auth username |
| `password` | `String` | SMTP auth password |
| `ssl` | `boolean` | Whether SSL-on-connect is enabled |
| `sslPort` | `int` | SSL port (default 465) |
| `securityMode` | `SecurityMode` | Enum expressing the transport security policy |
| `connectionTimeout` | `int` | Socket connection timeout in ms (default 10,000) |
| `socketTimeout` | `int` | Socket read timeout in ms (default 60,000) |

### Defaults

- `DEFAULT_SSL_PORT = 465`
- `DEFAULT_CONNECTION_TIMEOUT = 10_000` ms
- `DEFAULT_SOCKET_TIMEOUT = 60_000` ms

### Factory Methods

- `SMTPConfig.forPlain(host, port, user, pass)` — plain SMTP, no encryption
- `SMTPConfig.forSsl(host, port, user, pass, sslPort)` — implicit TLS (SMTPS)
- `SMTPConfig.forStartTlsOptional(host, port, user, pass)` — upgrade to TLS if available
- `SMTPConfig.forStartTlsRequired(host, port, user, pass)` — fail if STARTTLS not offered

### SecurityMode Enum

```
PLAIN | SSL | STARTTLS_OPTIONAL | STARTTLS_REQUIRED
```

The CLI flags map to these modes: `--sslon` → SSL, `--starttls` → STARTTLS_OPTIONAL, `--starttls-required` → STARTTLS_REQUIRED.

### Validation

The private constructor validates that `hostname` is non-null and non-blank, and that `port` is in the range 1–65535. Violations throw `IllegalArgumentException`.

### Secure Logging

- `toString()` redacts the username (`[REDACTED]` when non-empty)
- `toSecureString()` reports `hasCredentials=true/false` instead of actual values

---

## EmailModel

**Source:** `src/main/java/com/softinstigate/ermes/mail/EmailModel.java`
**Source Map:** See [EmailModel.java](../source-map.md#emailmodeljava) for change guidance.

Represents an email message with sender, subject, HTML body, recipients, and attachments.

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `from` | `String` | Sender email address |
| `senderFullName` | `String` | Sender display name (optional) |
| `subject` | `String` | Email subject |
| `message` | `String` | HTML body content |
| `to` | `List<Recipient>` | TO recipients (private, accessed via getters) |
| `cc` | `List<Recipient>` | CC recipients |
| `bcc` | `List<Recipient>` | BCC recipients |
| `attachments` | `List<Attachment>` | URL-based attachments |

### Constructor Validation

The constructor requires `from`, `subject`, and `message` to be non-null (throws `NullPointerException` otherwise). `senderFullName` may be null.

### Inner Records

**Recipient** — `record Recipient(String email, String name)`. Name is optional (null for address-only recipients). The compact constructor validates that `email` is non-null.

**Attachment** — `record Attachment(String url, String fileName, String description)`. Attachments are URL-based (not file-based); the URL is converted to `java.net.URI` then `java.net.URL` for Commons Email. Both `url` and `fileName` are validated as non-null in the compact constructor.

### Builder-Style Methods

- `addTo(email, name)` / `addCc(...)` / `addBcc(...)` — add single recipient
- `setMultipleTo(List<String>)` / `setMultipleCc(...)` / `setMultipleBcc(...)` — bulk add from email-only lists
- `addAttachment(url, fileName, description)` — add URL-based attachment
- `setTo(...)` / `setCc(...)` / `setBcc(...)` / `setAttachments(...)` — replace entire lists

### Unmodifiable Snapshots

All getter methods (`getToRecipients()`, `getCcRecipients()`, `getBccRecipients()`, `getAttachments()`) return unmodifiable snapshot lists via `List.copyOf()`. Mutating the returned list throws `UnsupportedOperationException`.

### Secure Logging

- `toString()` redacts the message body (`message='[REDACTED]'`)
- `toSecureString()` reports metadata only: subject length, message length, recipient counts (to/cc/bcc), attachment count

---

## EmailService

**Source:** `src/main/java/com/softinstigate/ermes/mail/EmailService.java`
**Source Map:** See [EmailService.java](../source-map.md#emailservicejava) for change guidance.

The primary API entry point. Manages an `ExecutorService` thread pool for async email delivery. Implements `AutoCloseable` for use with try-with-resources.

### Constructors

```java
EmailService(SMTPConfig smtpConfig)                             // pool size = availableProcessors()
EmailService(SMTPConfig smtpConfig, int threadPoolSize)         // explicit size (0 = sync only)
```

- `Objects.requireNonNull(smtpConfig)` — throws `NullPointerException` if null
- `IllegalArgumentException` if `threadPoolSize < 0`
- The thread pool is created **lazily** on the first `send()` call, not in the constructor

### Methods

- `send(EmailModel)` — async. Submits a `SendEmailTask` to the executor, returns `Future<List<String>>` (error list).
- `sendSynch(EmailModel)` — sync. Calls `SendEmailTask.call()` directly on the calling thread, returns `List<String>`.
- `shutdown()` — graceful shutdown with 10-second timeout.
- `shutdown(long timeout)` — graceful shutdown with custom timeout (seconds).
- `close()` — equivalent to `shutdown()` (AutoCloseable).

### send() After shutdown() Throws IllegalStateException

When `threadPoolSize > 0`, calling `send()` after `shutdown()` throws `IllegalStateException` with the message "Cannot send email: EmailService has been shut down". This is because the executor's `isShutdown()` check fails. When `threadPoolSize == 0`, there is no executor, so `send()` continues to work even after `shutdown()` (which is a no-op).

### Thread poolSize = 0

When `threadPoolSize == 0`, no executor is created. `send()` executes synchronously and returns an already-completed `Future`. This is useful when the caller manages concurrency externally (e.g., virtual threads). `shutdown()` is a no-op.

### Shutdown Behavior

`shutdown(long)` calls `executor.shutdown()` then `awaitTermination()`. If the timeout elapses, it calls `shutdownNow()` and logs the number of abandoned tasks. If interrupted during `awaitTermination()`, it forces `shutdownNow()` and re-interrupts the current thread. If the executor was never created (lazy init not triggered or `threadPoolSize == 0`), shutdown is a no-op.

### Thread Safety

`EmailService` is safe to share across threads. The `ExecutorService` handles concurrent task submission. Each `send()` call creates a new `SendEmailTask` instance. The executor field is `volatile` and initialized via a `synchronized` getter.

---

## SendEmailTask

**Source:** `src/main/java/com/softinstigate/ermes/mail/SendEmailTask.java`
**Source Map:** See [SendEmailTask.java](../source-map.md#sendemailtaskjava) for change guidance.

Implements `Callable<List<String>>`. Configures and sends a single email via Apache Commons Email `HtmlEmail`.

### Responsibilities

1. Sets up the `MailcapCommandMap` workaround for `javax.activation` MIME type resolution (once per JVM, guarded by `AtomicBoolean`)
2. Creates `HtmlEmail` via `HtmlEmailFactory` (injectable for testing)
3. Configures host, port, auth, SSL, STARTTLS from `SMTPConfig`
4. Sets HTML body, subject, from address from `EmailModel`
5. Processes attachments (URL-based, converted to `EmailAttachment`)
6. Adds TO/CC/BCC recipients
7. Calls `email.send()` and collects any errors into the error list

### Error Handling Contract

The `call()` method returns a `List<String>` of error messages. An empty list indicates success. Errors are collected as follows:

- **`EmailException`** — caught from `email.send()` or attachment/recipient operations. The exception message is added to the errors list. This is the expected failure mode for SMTP transport errors.
- **`RuntimeException`** — caught as a top-level safety net for unexpected errors (e.g., configuration issues, classpath problems). The message is prefixed with "Unexpected error: " and added to the errors list.
- **`IllegalArgumentException`** — caught from `URI.create()` during attachment processing when the URL string is not a valid URI. The error message includes the invalid URI and the exception detail.
- **`MalformedURLException`** — caught from `URI.toURL()` when the URI scheme is unsupported or the URL is otherwise malformed.
- **Null/blank attachment URLs** — detected before URI parsing; an error message is added and the attachment is skipped.

Errors do **not** abort the task. Attachment processing continues through remaining attachments even after individual failures. The task always returns the accumulated error list rather than throwing.

### STARTTLS Configuration

When `SMTPConfig.securityMode` is `STARTTLS_OPTIONAL` or `STARTTLS_REQUIRED`, the task calls:
- `email.setStartTLSEnabled(true)`
- `email.setStartTLSRequired(true)` (only for STARTTLS_REQUIRED)

### Context ClassLoader Swap

The task temporarily sets the thread's context classloader to `EmailService.class.getClassLoader()` before configuring and sending the email, restoring the original in a `finally` block. This works around classloader visibility issues in containerized or plugin-based environments.

### Debug Mode

If the system property `mail.debug` is `true`, the task enables JavaMail debug output. This is used in integration tests to capture TLS handshake evidence.

---

## HtmlEmailFactory

**Source:** `src/main/java/com/softinstigate/ermes/mail/HtmlEmailFactory.java`
<!-- openwiki: broken internal link [../source-map.md#htmlEmailfactoryjava] heading anchor "htmlEmailfactoryjava" does not exist in "../source-map.md". Fix the href or restore the target, then delete this comment. -->
**Source Map:** See [HtmlEmailFactory.java](../source-map.md#htmlEmailfactoryjava) for change guidance.

A single-method interface for creating `HtmlEmail` instances.

```java
public interface HtmlEmailFactory {
    HtmlEmail create();
}
```

### Testability Pattern

`HtmlEmailFactory` exists to decouple `SendEmailTask` from the concrete `HtmlEmail` constructor. This is a classic **factory injection** pattern for testability:

- **Production code** uses `DefaultHtmlEmailFactory`, which simply returns `new HtmlEmail()`.
- **Tests** inject a lambda or Mockito mock (`() -> mockEmail`) as the factory, allowing verification of method calls on `HtmlEmail` without a live SMTP connection.

The factory is accepted as the fourth constructor parameter of `SendEmailTask`:

```java
public SendEmailTask(SMTPConfig smtpConfig, EmailModel model, String charset, HtmlEmailFactory emailFactory)
```

The three-argument constructor (`SMTPConfig`, `EmailModel`, `String`) delegates to the four-argument constructor with `new DefaultHtmlEmailFactory()`, so production callers never need to supply a factory explicitly.

This pattern was introduced in v2.0.0 to allow `SendEmailTask` to be tested without a live SMTP connection. Tests in `SendEmailTaskTest` use this to verify STARTTLS configuration, SSL settings, error handling, and recipient/attachment wiring via Mockito `verify()` calls.

### DefaultHtmlEmailFactory

**Source:** `src/main/java/com/softinstigate/ermes/mail/DefaultHtmlEmailFactory.java`

The production implementation. Its `create()` method returns `new HtmlEmail()`.

---

## Main (CLI)

**Source:** `src/main/java/com/softinstigate/ermes/mail/Main.java`
**Source Map:** See [Main.java](../source-map.md#mainjava) for change guidance.

Picocli-based CLI entry point. Implements `Callable<Integer>` with annotated fields for all CLI flags.

### Key Behaviors

- Validates that `--sslon` and `--starttls` are mutually exclusive
- Warns if `--starttls-required` is set without `--starttls` (treats as plain SMTP)
- Creates `SMTPConfig` via the appropriate factory method based on flags
- Creates `EmailModel` from CLI args, sets recipients
- Uses `EmailService` with pool size 1 (effectively synchronous)
- Returns exit code 0 on success, 1 on error
- `--password` supports interactive prompting (picocli `arity = "0..1", interactive = true`)

### CLI Flag Summary

| Flag | Default | Description |
|------|---------|-------------|
| `-h, --host` | `localhost` | SMTP host |
| `-p, --port` | `25` | SMTP port |
| `-u, --user` | `""` | SMTP username |
| `-P, --password` | `""` | SMTP password (interactive prompt) |
| `--sslon` | `false` | Enable SSL-on-connect |
| `--sslport` | `465` | SSL port |
| `--starttls` | `false` | Enable STARTTLS |
| `--starttls-required` | `false` | Require STARTTLS |
| `-f, --from` | *(required)* | Sender address |
| `-n, --sender` | *(optional)* | Sender display name |
| `-s, --subject` | *(required)* | Email subject |
| `-b, --body` | *(required)* | HTML message body |
| `--to` | *(required)* | Comma-separated TO recipients |
| `--cc` | *(optional)* | Comma-separated CC recipients |
| `--bcc` | *(optional)* | Comma-separated BCC recipients |

### Version Provider

`VersionProvider.java` reads `Implementation-Version` from the JAR manifest (set by Maven) and formats a multi-line version display including Picocli version, JVM info, and OS info.
