---
type: Domain
title: ErmesMail Domain Concepts
description: Core domain objects in ErmesMail — SMTPConfig, EmailModel, EmailService, SendEmailTask, and the SecurityMode enum.
tags: [domain, smtp, email, config, model]
---

# Domain Concepts

## SMTPConfig

**Source:** `src/main/java/com/softinstigate/ermes/mail/SMTPConfig.java`

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

### Secure Logging

- `toString()` redacts the username (`[REDACTED]` when non-empty)
- `toSecureString()` reports `hasCredentials=true/false` instead of actual values

---

## EmailModel

**Source:** `src/main/java/com/softinstigate/ermes/mail/EmailModel.java`

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

### Inner Classes

**Recipient** — `{email, name}` tuple. Name is optional (null for address-only recipients).

**Attachment** — `{url, fileName, description}`. Attachments are URL-based (not file-based); the URL is converted to `java.net.URI` then `java.net.URL` for Commons Email.

### Builder-Style Methods

- `addTo(email, name)` / `addCc(...)` / `addBcc(...)` — add single recipient
- `setMultipleTo(List<String>)` / `setMultipleCc(...)` / `setMultipleBcc(...)` — bulk add from email-only lists
- `addAttachment(url, fileName, description)` — add URL-based attachment
- `setTo(...)` / `setCc(...)` / `setBcc(...)` / `setAttachments(...)` — replace entire lists

### Secure Logging

- `toString()` redacts the message body (`message='[REDACTED]'`)
- `toSecureString()` reports metadata only: subject length, recipient counts (to/cc/bcc), attachment count

---

## EmailService

**Source:** `src/main/java/com/softinstigate/ermes/mail/EmailService.java`

The primary API entry point. Manages an `ExecutorService` thread pool for async email delivery.

### Constructor

```java
EmailService(SMTPConfig smtpConfig, int threadPoolSize)
```

Creates a fixed thread pool of the given size. Logs the SMTP config (via `toSecureString()`) at initialization.

### Methods

- `send(EmailModel)` — async. Submits a `SendEmailTask` to the executor, returns `Future<List<String>>` (error list).
- `sendSynch(EmailModel)` — sync. Calls `SendEmailTask.call()` directly on the calling thread, returns `List<String>`.
- `shutdown()` — graceful shutdown with 10-second timeout.
- `shutdown(long timeout)` — graceful shutdown with custom timeout (seconds).

### Thread Safety

`EmailService` is safe to share across threads. The `ExecutorService` handles concurrent task submission. Each `send()` call creates a new `SendEmailTask` instance.

---

## SendEmailTask

**Source:** `src/main/java/com/softinstigate/ermes/mail/SendEmailTask.java`

Implements `Callable<List<String>>`. Configures and sends a single email via Apache Commons Email `HtmlEmail`.

### Responsibilities

1. Sets up the `MailcapCommandMap` workaround for `javax.activation` MIME type resolution
2. Creates `HtmlEmail` via `HtmlEmailFactory` (injectable for testing)
3. Configures host, port, auth, SSL, STARTTLS from `SMTPConfig`
4. Sets HTML body, subject, from address from `EmailModel`
5. Processes attachments (URL-based, converted to `EmailAttachment`)
6. Adds TO/CC/BCC recipients
7. Calls `email.send()` and collects any `EmailException` into the error list

### STARTTLS Configuration

When `SMTPConfig.securityMode` is `STARTTLS_OPTIONAL` or `STARTTLS_REQUIRED`, the task calls:
- `email.setStartTLSEnabled(true)`
- `email.setStartTLSRequired(true)` (only for STARTTLS_REQUIRED)

### Debug Mode

If the system property `mail.debug` is `true`, the task enables JavaMail debug output. This is used in integration tests to capture TLS handshake evidence.

---

## HtmlEmailFactory

**Source:** `src/main/java/com/softinstigate/ermes/mail/HtmlEmailFactory.java`

A single-method interface for creating `HtmlEmail` instances. Exists solely for testability — production code uses `DefaultHtmlEmailFactory`, tests inject a Mockito mock.

```java
public interface HtmlEmailFactory {
    HtmlEmail create();
}
```

This pattern was introduced in v2.0.0 to allow `SendEmailTask` to be tested without a live SMTP connection.

---

## Main (CLI)

**Source:** `src/main/java/com/softinstigate/ermes/mail/Main.java`

Picocli-based CLI entry point. Implements `Callable<Integer>` with annotated fields for all CLI flags.

### Key Behaviors

- Validates that `--sslon` and `--starttls` are mutually exclusive
- Creates `SMTPConfig` via the appropriate factory method based on flags
- Creates `EmailModel` from CLI args, sets recipients
- Uses `EmailService` with pool size 1 (effectively synchronous)
- Returns exit code 0 on success, 1 on error
- `--password` supports interactive prompting (picocli `arity = "0..1", interactive = true`)

### Version Provider

`VersionProvider.java` reads `Implementation-Version` from the JAR manifest (set by Maven) and formats a multi-line version display including Picocli version, JVM info, and OS info.
