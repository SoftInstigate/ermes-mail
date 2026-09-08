---
type: Source Map
title: ErmesMail Source Map
description: File-by-file guide to every production class, test class, build file, and script in ErmesMail with role descriptions, key behaviors, change guidance, and task routing.
tags: [source-map, java, codebase, navigation, mail]
verified:
  - by: openwiki/0.5.0
    at: 2026-09-08T09:00:18.765Z
sources:
  - id: openwiki-source-164e2da859b5277df81c7d94
    resource: repo://.github/workflows/ci.yml
  - id: openwiki-source-4a6c16f0a98b1c8c5fe325d8
    resource: repo://.github/workflows/maven-publish.yml
  - id: openwiki-source-9a509e4f28155fbfb293d571
    resource: repo://setversion.sh
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
  - id: openwiki-source-f0b50adc5b590b7d1e252df2
    resource: repo://src/main/java/com/softinstigate/ermes/mail/VersionProvider.java
  - id: openwiki-source-2d7799a9989ae46bf347e54a
    resource: repo://src/test/java/com/softinstigate/ermes/mail/DefaultHtmlEmailFactoryTest.java
  - id: openwiki-source-0ece14d1aa35917f64939a50
    resource: repo://src/test/java/com/softinstigate/ermes/mail/EmailModelTest.java
  - id: openwiki-source-c1b9dac4ccfdcefca48255c3
    resource: repo://src/test/java/com/softinstigate/ermes/mail/EmailServiceTest.java
  - id: openwiki-source-bf6289d8d59b7e696ef23f13
    resource: repo://src/test/java/com/softinstigate/ermes/mail/IntegrationScenariosIT.java
  - id: openwiki-source-b31fc1816013f42827333e96
    resource: repo://src/test/java/com/softinstigate/ermes/mail/MainCliTest.java
  - id: openwiki-source-3d6eb9099e3c0ec4cef2e98c
    resource: repo://src/test/java/com/softinstigate/ermes/mail/SendEmailTaskTest.java
  - id: openwiki-source-2e9051b2ab4dcf828e614778
    resource: repo://src/test/java/com/softinstigate/ermes/mail/SMTPConfigTest.java
  - id: openwiki-source-8538a375cf4827a2b63a0319
    resource: repo://update-dependencies.sh
generated: { by: "openwiki/0.5.0", at: "2026-09-08T09:00:18.765Z" }
---

# Source Map

## Production Code

All production classes are in `src/main/java/com/softinstigate/ermes/mail/`.

### Main.java {#mainjava}

**Role:** CLI entry point.  
**Framework:** picocli (`@Command`, `Callable<Integer>`).  
**Key behaviors:**
- Parses all CLI flags (host, port, user, password, security mode, from, sender, to, cc, bcc, subject, body)
- Validates mutual exclusivity of `--sslon` and `--starttls`; warns when `--starttls-required` is set without `--starttls`
- Creates `SMTPConfig` via factory methods based on flag combinations
- Creates `EmailModel` and sets recipients using `setMultipleTo/Cc/Bcc`
- Uses `EmailService` with pool size 1 (effectively synchronous for CLI)
- Returns exit code 1 on errors or mutual-exclusion violation

**When changing:** If adding new CLI flags, update the `@Option`-annotated fields and the `call()` method. The `--password` flag uses `interactive = true` with `arity = "0..1"` for secure prompting. Recipient flags (`--to`, `--cc`, `--bcc`) use `split = ","` for comma-separated lists.

### EmailService.java {#emailservicejava}

**Role:** Public API for sending emails.  
**Key behaviors:**
- Manages a `java.util.concurrent.ExecutorService` (fixed thread pool, created **lazily** on first `send()` call)
- `send(EmailModel)` — async, returns `Future<List<String>>`; with `threadPoolSize=0`, executes synchronously and returns a completed `CompletableFuture`
- `sendSynch(EmailModel)` — sync, returns `List<String>`
- `shutdown()` / `shutdown(long)` — graceful executor termination (no-op if pool was never created)
- Implements `AutoCloseable` (`close()` delegates to `shutdown()`)
- Default constructor sets pool size to `Runtime.getRuntime().availableProcessors()`

**When changing:** Thread pool lifecycle is critical. `threadPoolSize=0` disables the pool entirely for external concurrency management (e.g., virtual threads). The 10-second default shutdown timeout is hardcoded — if callers need longer, they use `shutdown(long)`. The constructor logs via `smtpConfig.toSecureString()`.

### EmailModel.java {#emailmodeljava}

**Role:** Email data model.  
**Key behaviors:**
- Holds sender info (`from`, `senderFullName`), subject, HTML body, recipients (TO/CC/BCC), attachments
- Builder-style methods for adding recipients and attachments (`addTo`, `addCc`, `addBcc`, `addAttachment`)
- Bulk replacement via `setMultipleTo/Cc/Bcc` (accept `List<String>`, email-only) and `setTo/Cc/Bcc` (accept `List<Recipient>`)
- Inner records: `Recipient` (email + name) and `Attachment` (url + fileName + description) with compact-constructor null validation
- `toString()` redacts message body; `toSecureString()` reports lengths/counts only
- All getter methods return unmodifiable snapshot lists (`List.copyOf`)

**When changing:** Recipients are stored in private `ArrayList`s, exposed as unmodifiable lists via getters. The `setMultipleTo/Cc/Bcc` methods accept `List<String>` (email-only, no names) and internally map to `Recipient` records.

### SMTPConfig.java {#smtpconfigjava}

**Role:** Immutable SMTP server configuration with explicit security mode.  
**Key behaviors:**
- Private constructor, public static factory methods: `forPlain`, `forSsl`, `forStartTlsOptional`, `forStartTlsRequired`
- `SecurityMode` enum: `PLAIN`, `SSL`, `STARTTLS_OPTIONAL`, `STARTTLS_REQUIRED`
- Validates hostname (not null/blank) and port (1–65535) in the private constructor
- `toString()` redacts username; `toSecureString()` omits credentials entirely, reports `hasCredentials` boolean
- `DEFAULT_SSL_PORT = 465`, `DEFAULT_CONNECTION_TIMEOUT = 10_000` ms, `DEFAULT_SOCKET_TIMEOUT = 60_000` ms
- Public final fields include `connectionTimeout` and `socketTimeout` for transport-level timeouts

**When changing:** Adding a new security mode requires a new factory method, a new enum value, and handling in `SendEmailTask.configureSecurity()` where STARTTLS/SSL flags are set on `HtmlEmail`.

### SendEmailTask.java {#sendemailtaskjava}

**Role:** `Callable<List<String>>` that sends a single email via Commons Email.  
**Key behaviors:**
- Configures `HtmlEmail` from `SMTPConfig` + `EmailModel` via private helper methods:
  - `configureCharsetAndServer` — charset, hostname, port, auth, socket timeouts
  - `configureSecurity` — SSL-on-connect, SSL port, STARTTLS based on `SecurityMode` switch
  - `configureSenderAndContent` — from, subject, HTML body
  - `processAttachments` — URL-based attachments via `EmailAttachment`
  - `addRecipients` — iterates TO/CC/BCC
- Includes `MailcapCommandMap` workaround (idempotent via `AtomicBoolean`) for javax.activation MIME type issues
- Respects `mail.debug` system property for JavaMail debug output
- Swaps thread context classloader to `EmailService`'s classloader during send, restoring in `finally`

**When changing:** This is where SMTP transport configuration happens. Changes to security mode handling must match the `SMTPConfig.SecurityMode` enum. The `HtmlEmailFactory` injection point is the fourth constructor parameter, used for testability.

### HtmlEmailFactory.java {#htmlemailfactoryjava}

**Role:** Interface for creating `HtmlEmail` instances.  
**Single method:** `HtmlEmail create()`  
**Purpose:** Dependency injection for testability — allows Mockito mocking of `HtmlEmail`.

### DefaultHtmlEmailFactory.java {#defaulthtmlemailfactoryjava}

**Role:** Production implementation of `HtmlEmailFactory`.  
**Single method:** `return new HtmlEmail()`

### VersionProvider.java {#versionproviderjava}

**Role:** Provides version info to picocli's `--version` flag.  
**Key behavior:** Reads `Implementation-Version` from the JAR manifest (set by Maven's `maven-jar-plugin` with `addDefaultImplementationEntries`). Returns multiple lines including ErmesMail version, Picocli version, JVM info, and OS info.

## Test Code

All test classes are in `src/test/java/com/softinstigate/ermes/mail/`.

### SMTPConfigTest.java

**Tests:** Factory methods produce correct `SecurityMode` and field values. Checks `toString()` output contains expected host and mode strings. Also validates `toSecureString()` redaction.

### SendEmailTaskTest.java

**Tests:** STARTTLS required/optional and SSL configuration are correctly applied to `HtmlEmail`. Uses Mockito mock of `HtmlEmail` injected via `HtmlEmailFactory`. Verifies `setSSLOnConnect`, `setSslSmtpPort`, `setStartTLSEnabled`, `setStartTLSRequired` are called (or not) as expected. Also verifies plain mode sets neither SSL nor STARTTLS.

### DefaultHtmlEmailFactoryTest.java

**Tests:** `DefaultHtmlEmailFactory.create()` returns a non-null `HtmlEmail` instance, multiple calls return distinct instances.

### EmailModelTest.java

**Tests:** Constructor field assignment, `addTo/addCc/addBcc`, `setMultipleTo/Cc/Bcc` replacement behavior, unmodifiable list returns from getters, `Recipient` and `Attachment` record null validation, secure logging (`toString()` redacts message, `toSecureString()` reports lengths/counts), and attachment management.

### EmailServiceTest.java

**Tests:** Constructor validation (null SMTPConfig rejection, negative thread pool rejection, zero pool size acceptance). Thread pool size 0 behavior (synchronous execution, no executor created). `send()` returns Future that completes with errors for unreachable port. `sendSynch()` returns errors for unreachable port. Null model rejection for both `send()` and `sendSynch()`.

### MainCliTest.java

**Tests:** CLI flag parsing via `CommandLine.populateCommand`. SSL flag maps to `SecurityMode.SSL`. STARTTLS required maps to `SecurityMode.STARTTLS_REQUIRED`. Mutual exclusivity detection (`--sslon` + `--starttls`). Exit code 1 for conflicting flags.

### IntegrationScenariosIT.java

**Tests:** Live SMTP delivery in two scenarios:
- `local-plain-mailpit` — plain SMTP to Mailpit on localhost:1025 (auto-skipped if not reachable, checked via socket connect)
- `external-smtps-conditional` — SSL/STARTTLS to external provider (auto-skipped if no credentials)

Uses `@TestFactory` with `DynamicTest` for conditional execution. Reads config from env vars (`SMTP_INTEGRATION_*`), `smtp-integration.properties`, or `.env`. Auto-detects STARTTLS vs implicit SSL via `SMTP_INTEGRATION_STARTTLS` env or port heuristic (587 → STARTTLS).

## Build & Configuration Files

### pom.xml

**Role:** Maven build descriptor.  
**Key config:**
- Java 17 source/target
- `maven-shade-plugin` produces `ermes-mail.jar` fat JAR with `Main` as manifest main class
- `maven-surefire-plugin` excludes `**/IT*.java` and loads ByteBuddy agent via `-javaagent`
- `maven-failsafe-plugin` runs integration tests with ByteBuddy agent
- `license-maven-plugin` manages Apache 2.0 headers
- `maven-jar-plugin` with `addDefaultImplementationEntries` for `VersionProvider`
- ByteBuddy agent dependency (`1.18.11-jdk5`) for Mockito inline mock-maker

### .github/workflows/ci.yml

**Role:** CI pipeline — runs unit tests on push to branches (path-filtered to `**/*.java` and `pom.xml`, tags ignored). Downloads ByteBuddy agent and runs `mvn test` with `-javaagent` on JDK 17 (Temurin).

### .github/workflows/maven-publish.yml

**Role:** Publishes to GitHub Packages on tag push. Runs full `mvn package` (including tests) before `mvn deploy` with `-DskipITs`. Uses `GITHUB_TOKEN` for authentication.

### .github/workflows/openwiki-update.yml

**Role:** Scheduled documentation refresh every 7 days (cron `29 4 */7 * *`) and on manual dispatch. Runs OpenWiki against the codebase and creates a PR with updated documentation.

### smtp-integration.properties.example

**Role:** Template for integration test SMTP credentials. Copy to `smtp-integration.properties` and fill in values (git-ignored).

### .gitignore

**Role:** Standard Maven/IDE ignores. Includes `smtp-integration.properties` to prevent credential commits.

### CHANGELOG.md

**Role:** Release notes following Keep a Changelog format. Documents breaking changes, migration guides, and security changes.

### setversion.sh

**Role:** Release versioning script. Safely sets the Maven project version with semver validation, branch-name checks, and git tagging.

**Key behaviors:**
- Accepts `major.minor.patch` or `major.minor.patch-SNAPSHOT` version formats
- Validates semver, detects downgrades, checks for existing tags
- Runs `mvn versions:set` to update pom.xml
- For release versions: commits, creates a git tag (e.g., `3.0.0`); requires branch `<major>.x`
- For SNAPSHOT versions: commits with `[skip ci]` suffix
- Supports `--dry-run` (preview only) and `--force` (override guards)
- Also updates `chart/Chart.yaml` for Helm-based deployments when present

**When changing:** If the release process or versioning policy changes, update this script and the corresponding [Operations Runbook](operations/runbook.md) section.

### update-dependencies.sh

**Role:** Dependency update script. Runs Maven `versions:use-latest-releases` and `versions:update-properties` to update pom.xml dependencies.

**Key behaviors:**
- Takes optional first argument (`true`/`false`) to control `allowMinorUpdates`
- Runs two Maven goals sequentially: `versions:use-latest-releases` then `versions:update-properties`
- Uses Maven wrapper (`mvnw`) if present in the project root

**When changing:** Use this script before a release cycle to update dependencies, then run `mvn test` to verify compatibility. Keep ByteBuddy in sync with Mockito.

### .github/copilot-instructions.md

**Role:** GitHub Copilot instructions for AI-assisted development in this repository. Covers architecture, key workflows, dependency patterns, and common pitfalls.

### LICENSE.txt

**Role:** Apache License 2.0 full text.

## Where to Start When...

| Task | Start Here |
|------|------------|
| Add a new CLI flag | `Main.java` — add `@Option` field, handle in `call()` |
| Add a new SMTP security mode | `SMTPConfig.java` (enum + factory), `SendEmailTask.java` (`configureSecurity`), `Main.java` (CLI flag) |
| Change email sending logic | `SendEmailTask.call()` and its private helper methods |
| Add a new recipient type | `EmailModel.java` — add list, getter, add/set methods |
| Debug SMTP connection issues | `SendEmailTask.call()` — enable `mail.debug=true` system property |
| Add a new integration test scenario | `IntegrationScenariosIT.java` — add `DynamicTest` to `scenarios()` |
| Update dependencies | `update-dependencies.sh` then verify with `mvn test`; keep ByteBuddy in sync with Mockito |
| Create a release | `setversion.sh <version>` — updates pom.xml, commits, tags; see [Operations Runbook](operations/runbook.md) |
| Change CI pipeline | `.github/workflows/ci.yml` — ByteBuddy agent version, Maven flags |
