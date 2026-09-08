---
type: Quickstart
title: ErmesMail Quickstart
description: Entry point for the ErmesMail code wiki. Covers what the project does, how to build and run it, and where to find detailed documentation.
tags: [quickstart, java, email, smtp, maven]
verified:
  - by: openwiki/0.5.0
    at: 2026-09-08T09:00:18.765Z
sources:
  - id: openwiki-source-2355f81d7cf522f8dbdaabd4
    resource: repo://pom.xml
  - id: openwiki-source-23775c3de52f3ab95a13cb8b
    resource: repo://README.md
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
generated: { by: "openwiki/0.5.0", at: "2026-09-08T09:00:18.765Z" }
---

# ErmesMail Quickstart

ErmesMail (Ἑρμῆς Mail) is a Java library and CLI tool for sending HTML emails asynchronously via SMTP. It wraps [Apache Commons Email](https://commons.apache.org/proper/commons-email/) with a clean API and adds a [picocli](https://picocli.info/)-based command-line interface for shell usage.

**Current version:** 3.0.1-SNAPSHOT (released: 3.0.0)  
**License:** Apache License 2.0  
**Organization:** [SoftInstigate srl](https://softinstigate.com)

## Two Ways to Use

1. **As a library** — embed in your Maven project and call `EmailService` programmatically
2. **As a CLI tool** — build a fat JAR and send emails from the shell

```mermaid
flowchart TD
    A[Start] --> B{Usage mode?}
    B -->|Library| C[Add Maven dependency]
    B -->|CLI| D[Build fat JAR]
    C --> E[Create SMTPConfig]
    D --> F[Run with flags]
    E --> G[Create EmailModel]
    F --> H[Parse CLI options]
    G --> I[Use EmailService]
    H --> I
    I --> J[Send email]
```

*Figure: Two paths to send email with ErmesMail — library (programmatic) and CLI (shell).*

## Build

```shell
mvn package
```

Produces `target/ermes-mail.jar` (shaded fat JAR with all dependencies).

## CLI Usage

```shell
java -jar target/ermes-mail.jar --help
```

Key flags:
- `-h/--host`, `-p/--port` — SMTP server (default: localhost:25)
- `-u/--user`, `-P/--password` — credentials
- `--sslon` + `--sslport` — implicit SSL (SMTPS, typically port 465)
- `--starttls` / `--starttls-required` — STARTTLS upgrade
- `-f/--from`, `-n/--sender`, `-s/--subject`, `-b/--body` — email content
- `--to`, `--cc`, `--bcc` — recipients (comma-separated)

**Quick test with Mailpit:**

```shell
# Start Mailpit (local SMTP mock), then:
java -jar target/ermes-mail.jar -h localhost -p 1025 \
  -f sender@email.com -s "Test" -b "<strong>Hello</strong>" \
  --to receiver@email.com
```

## Library Usage

Add JitPack repository and the `ermes-mail:3.0.0:shaded` dependency to your pom.xml. See [README.md](../README.md) for the full Maven snippet and javax.mail version warnings.

```java
SMTPConfig smtpConfig = SMTPConfig.forPlain("localhost", 1025, "user", "password");
EmailModel emailModel = new EmailModel("from@ex.com", "Sender", "Subject", "<b>Body</b>");
emailModel.addTo("to@ex.com", "Recipient");

try (EmailService emailService = new EmailService(smtpConfig)) {
    Future<List<String>> errors = emailService.send(emailModel); // async, pool created lazily
    // or: List<String> errors = emailService.sendSynch(emailModel); // synchronous
}
```

Key 3.0 features:
- **Lazy thread pool** — the `ExecutorService` is created only on the first `send()` call
- **Virtual threads support** — `threadPoolSize=0` disables internal pool, letting callers manage concurrency externally
- **Socket timeouts** — configurable `connectionTimeout` and `socketTimeout` in `SMTPConfig`
- **`AutoCloseable`** — `EmailService` implements `AutoCloseable` for try-with-resources
- **Input validation** — early `NullPointerException`/`IllegalArgumentException` on invalid inputs

## Key Documentation Pages

| Page | What It Covers |
|------|----------------|
| [Architecture Overview](architecture/overview.md) | Package structure, class relationships, data flow |
| [Domain Concepts](domain/concepts.md) | SMTPConfig, EmailModel, EmailService, SecurityMode |
| [Source Map](source-map.md) | File-by-file guide to the codebase |
| [Testing Guide](testing/guide.md) | Unit tests, integration tests, mocking patterns |
| [Operations](operations/runbook.md) | SMTP configuration, CI/CD, troubleshooting |

## Task Routing

| Change Area | Wiki Page | Source Entry Point | Key Symbols | Focused Tests | Validation |
|-------------|-----------|-------------------|-------------|---------------|------------|
| Add CLI flag | [Source Map](source-map.md), [Domain](domain/concepts.md) | `Main.java` | `@Option` fields, `call()` | `MainCliTest.java` | `mvn test` |
| New SMTP security mode | [Architecture](architecture/overview.md), [Domain](domain/concepts.md) | `SMTPConfig.java`, `SendEmailTask.java` | `SecurityMode` enum, `forXxx()` factory, `configureSecurity()` | `SMTPConfigTest.java`, `SendEmailTaskTest.java` | `mvn test` |
| Change email sending logic | [Domain](domain/concepts.md) | `SendEmailTask.call()` | `configureSenderAndContent()`, `processAttachments()` | `SendEmailTaskTest.java` | `mvn test` |
| Thread pool / concurrency | [Architecture](architecture/overview.md), [Domain](domain/concepts.md) | `EmailService.java` | `send()`, `sendSynch()`, `shutdown()`, `getExecutor()` | `EmailServiceTest.java` | `mvn test` |
| Recipient/Attachment model | [Domain](domain/concepts.md) | `EmailModel.java` | `Recipient` record, `Attachment` record | `EmailModelTest.java` | `mvn test` |
| HtmlEmailFactory seam | [Architecture](architecture/overview.md), [Domain](domain/concepts.md) | `HtmlEmailFactory.java`, `DefaultHtmlEmailFactory.java` | `HtmlEmailFactory` interface, `DefaultHtmlEmailFactory` implementation | `SendEmailTaskTest.java`, `DefaultHtmlEmailFactoryTest.java` | `mvn test` |
| Integration test scenario | [Testing](testing/guide.md) | `IntegrationScenariosIT.java` | `scenarios()`, `DynamicTest` | `IntegrationScenariosIT.java` | `mvn verify` |
| CI pipeline | [Operations](operations/runbook.md) | `.github/workflows/ci.yml` | `BYTEBUDDY_VERSION`, `argLine` | — | push to branch |
| Dependency update | [Operations](operations/runbook.md) | `pom.xml`, `update-dependencies.sh` | `bytebuddy.version` property | all tests | `mvn test` |
| Release / version bump | [Operations](operations/runbook.md) | `setversion.sh` | `pom.xml` version, git tag | `mvn package` | `./setversion.sh <ver> --dry-run` |

## Project Structure (Quick Reference)

```
src/main/java/com/softinstigate/ermes/mail/
  Main.java              — CLI entry point (picocli)
  EmailService.java      — Async/sync email sender (ExecutorService)
  EmailModel.java        — Email data model (recipients, attachments)
  SMTPConfig.java        — SMTP server config + security mode
  SendEmailTask.java     — Callable that sends via Commons Email HtmlEmail
  HtmlEmailFactory.java  — Abstraction for testability
  DefaultHtmlEmailFactory.java — Production factory
  VersionProvider.java   — CLI version display

src/test/java/com/softinstigate/ermes/mail/
  SMTPConfigTest.java          — Unit tests for config factories
  SendEmailTaskTest.java       — Unit tests with Mockito
  DefaultHtmlEmailFactoryTest.java — Factory contract test
  EmailModelTest.java          — Model validation and secure logging tests
  EmailServiceTest.java        — Thread pool lifecycle tests
  MainCliTest.java             — CLI flag parsing tests
  IntegrationScenariosIT.java  — Live SMTP integration tests

setversion.sh                  — Release versioning script (semver + git tag)
update-dependencies.sh         — Maven dependency update script
```
