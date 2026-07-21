---
type: Source Map
title: ErmesMail Source Map
description: File-by-file guide to the ErmesMail codebase with descriptions, key responsibilities, and change guidance.
tags: [source-map, java, codebase, navigation]
---

# Source Map

## Production Code

All production classes are in `src/main/java/com/softinstigate/ermes/mail/`.

### Main.java

**Role:** CLI entry point.  
**Framework:** picocli (`@Command`, `Callable<Integer>`).  
**Key behaviors:**
- Parses all CLI flags (host, port, user, password, security mode, from, to, cc, bcc, subject, body)
- Validates mutual exclusivity of `--sslon` and `--starttls`
- Creates `SMTPConfig` via factory methods based on flag combinations
- Creates `EmailModel` and sets recipients
- Uses `EmailService` with pool size 1 (effectively synchronous for CLI)

**When changing:** If adding new CLI flags, update the `@Option`-annotated fields and the `call()` method. The `--password` flag uses `interactive = true` for secure prompting.

### EmailService.java

**Role:** Public API for sending emails.  
**Key behaviors:**
- Manages a `java.util.concurrent.ExecutorService` (fixed thread pool)
- `send(EmailModel)` — async, returns `Future<List<String>>`
- `sendSynch(EmailModel)` — sync, returns `List<String>`
- `shutdown()` / `shutdown(long)` — graceful executor termination

**When changing:** Thread pool lifecycle is critical. The 10-second default shutdown timeout is hardcoded — if callers need longer, they use `shutdown(long)`. The constructor logs via `smtpConfig.toSecureString()`.

### EmailModel.java

**Role:** Email data model.  
**Key behaviors:**
- Holds sender info, subject, HTML body, recipients (TO/CC/BCC), attachments
- Builder-style methods for adding recipients and attachments
- Inner classes: `Recipient` (email + name), `Attachment` (url + fileName + description)
- `toSecureString()` for safe logging (reports metadata only)

**When changing:** Recipients are stored in private `ArrayList`s, exposed as unmodifiable lists via getters. The `setMultipleTo/Cc/Bcc` methods accept `List<String>` (email-only, no names).

### SMTPConfig.java

**Role:** SMTP server configuration with explicit security mode.  
**Key behaviors:**
- Private constructor, public static factory methods
- `SecurityMode` enum: `PLAIN`, `SSL`, `STARTTLS_OPTIONAL`, `STARTTLS_REQUIRED`
- `toString()` redacts username; `toSecureString()` omits credentials entirely
- `DEFAULT_SSL_PORT = 465`

**When changing:** Adding a new security mode requires a new factory method, a new enum value, and handling in `SendEmailTask.call()` where STARTTLS/SSL flags are set on `HtmlEmail`.

### SendEmailTask.java

**Role:** `Callable` that sends a single email via Commons Email.  
**Key behaviors:**
- Configures `HtmlEmail` from `SMTPConfig` + `EmailModel`
- Handles SSL (`setSSLOnConnect`), STARTTLS (`setStartTLSEnabled`, `setStartTLSRequired`)
- Processes URL-based attachments
- Includes `MailcapCommandMap` workaround for javax.activation MIME type issues
- Respects `mail.debug` system property for JavaMail debug output

**When changing:** This is where SMTP transport configuration happens. Changes to security mode handling must match the `SMTPConfig.SecurityMode` enum. The `HtmlEmailFactory` injection point is the fourth constructor parameter.

### HtmlEmailFactory.java

**Role:** Interface for creating `HtmlEmail` instances.  
**Single method:** `HtmlEmail create()`  
**Purpose:** Dependency injection for testability — allows Mockito mocking of `HtmlEmail`.

### DefaultHtmlEmailFactory.java

**Role:** Production implementation of `HtmlEmailFactory`.  
**Single method:** `return new HtmlEmail()`

### VersionProvider.java

**Role:** Provides version info to picocli's `--version` flag.  
**Key behavior:** Reads `Implementation-Version` from the JAR manifest (set by Maven's `maven-jar-plugin` with `addDefaultImplementationEntries`).

## Test Code

All test classes are in `src/test/java/com/softinstigate/ermes/mail/`.

### SMTPConfigTest.java

**Tests:** Factory methods produce correct `SecurityMode` and field values. Checks `toString()` output.

### SendEmailTaskTest.java

**Tests:** STARTTLS and SSL configuration is correctly applied to `HtmlEmail`. Uses Mockito mock of `HtmlEmail` injected via `HtmlEmailFactory`.

### MainCliTest.java

**Tests:** CLI flag parsing and validation.

### IntegrationScenariosIT.java

**Tests:** Live SMTP delivery in two scenarios:
- `local-plain-mailpit` — plain SMTP to Mailpit (auto-skipped if not running)
- `external-smtps-conditional` — SSL/STARTTLS to external provider (auto-skipped if no credentials)

Uses `@TestFactory` with `DynamicTest` for conditional execution. Reads config from env vars, `smtp-integration.properties`, or `.env`.

## Build & Configuration Files

### pom.xml

**Role:** Maven build descriptor.  
**Key config:**
- Java 17 source/target
- `maven-shade-plugin` produces `ermes-mail.jar` fat JAR
- `maven-surefire-plugin` excludes `**/IT*.java`
- `maven-failsafe-plugin` runs integration tests
- `license-maven-plugin` manages Apache 2.0 headers
- ByteBuddy agent dependency for Mockito inline mock-maker

### .github/workflows/ci.yml

**Role:** CI pipeline — runs unit tests on push to branches.

### .github/workflows/maven-publish.yml

**Role:** Publishes to GitHub Packages on tag push.

### .github/workflows/openwiki-update.yml

**Role:** Scheduled documentation refresh.

### smtp-integration.properties.example

**Role:** Template for integration test SMTP credentials. Copy to `smtp-integration.properties` and fill in values (git-ignored).

### .gitignore

**Role:** Standard Maven/IDE ignores. Includes `smtp-integration.properties` to prevent credential commits.

### CHANGELOG.md

**Role:** Release notes following Keep a Changelog format. Documents breaking changes, migration guides, and security changes.

### LICENSE.txt

**Role:** Apache License 2.0 full text.

## Where to Start When...

| Task | Start Here |
|------|------------|
| Add a new CLI flag | `Main.java` — add `@Option` field, handle in `call()` |
| Add a new SMTP security mode | `SMTPConfig.java` (enum + factory), `SendEmailTask.java` (HtmlEmail config), `Main.java` (CLI flag) |
| Change email sending logic | `SendEmailTask.call()` |
| Add a new recipient type | `EmailModel.java` — add list, getter, add/set methods |
| Debug SMTP connection issues | `SendEmailTask.call()` — enable `mail.debug=true` |
| Add a new integration test scenario | `IntegrationScenariosIT.java` — add `DynamicTest` to `scenarios()` |
| Update dependencies | `pom.xml` — keep ByteBuddy in sync with Mockito |
