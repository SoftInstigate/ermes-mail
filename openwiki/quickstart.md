---
type: Quickstart
title: ErmesMail Quickstart
description: Entry point for the ErmesMail code wiki. Covers what the project does, how to build and run it, and where to find detailed documentation.
tags: [quickstart, java, email, smtp, maven]
---

# ErmesMail Quickstart

ErmesMail (Ἑρμῆς Mail) is a Java library and CLI tool for sending HTML emails asynchronously via SMTP. It wraps [Apache Commons Email](https://commons.apache.org/proper/commons-email/) with a clean API and adds a [picocli](https://picocli.info/)-based command-line interface for shell usage.

**Current version:** 2.1.1-SNAPSHOT (released: 2.1.0)  
**License:** Apache License 2.0  
**Organization:** [SoftInstigate srl](https://softinstigate.com)

## Two Ways to Use

1. **As a library** — embed in your Maven project and call `EmailService` programmatically
2. **As a CLI tool** — build a fat JAR and send emails from the shell

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

Add JitPack repository and the `ermes-mail:2.1.0:shaded` dependency to your pom.xml. See [README.md](../README.md) for the full Maven snippet and javax.mail version warnings.

```java
SMTPConfig smtpConfig = SMTPConfig.forPlain("localhost", 1025, "user", "password");
EmailModel emailModel = new EmailModel("from@ex.com", "Sender", "Subject", "<b>Body</b>");
emailModel.addTo("to@ex.com", "Recipient");

EmailService emailService = new EmailService(smtpConfig, 3);
Future<List<String>> errors = emailService.send(emailModel); // async
emailService.shutdown();
```

## Key Documentation Pages

| Page | What It Covers |
|------|----------------|
| [Architecture Overview](architecture/overview.md) | Package structure, class relationships, data flow |
| [Domain Concepts](domain/concepts.md) | SMTPConfig, EmailModel, EmailService, SecurityMode |
| [Source Map](source-map.md) | File-by-file guide to the codebase |
| [Testing Guide](testing/guide.md) | Unit tests, integration tests, mocking patterns |
| [Operations](operations/runbook.md) | SMTP configuration, CI/CD, troubleshooting |

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
  MainCliTest.java             — CLI flag parsing tests
  IntegrationScenariosIT.java  — Live SMTP integration tests
```

## Backlog

- **Recipient/Attachment model docs** — inner classes `Recipient`, `Attachment` in `EmailModel` could use standalone documentation if the model grows more complex. Source: `EmailModel.java`.
- **Commons Email integration details** — deeper documentation of HtmlEmail lifecycle, attachment handling, and the `MailcapCommandMap` workaround in `SendEmailTask`. Source: `SendEmailTask.java`.
- **GitHub Actions workflow details** — CI, Maven Publish, and OpenWiki Update workflows are documented at a high level but could benefit from a dedicated operations page expansion. Source: `.github/workflows/`.
