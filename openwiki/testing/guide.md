---
type: Testing
title: ErmesMail Testing Guide
description: How to run unit and integration tests, mocking patterns for SendEmailTask, and CI configuration for ErmesMail.
tags: [testing, junit, mockito, integration, ci]
---

# Testing Guide

## Test Structure

```
src/test/java/com/softinstigate/ermes/mail/
  SMTPConfigTest.java          — Unit tests for SMTPConfig factory methods
  SendEmailTaskTest.java       — Unit tests for SendEmailTask (mocked HtmlEmail)
  MainCliTest.java             — CLI flag parsing tests
  IntegrationScenariosIT.java  — Live SMTP integration tests
```

## Running Tests

### Unit Tests Only

```shell
mvn test
```

Unit tests exclude files matching `**/IT*.java` (configured in `maven-surefire-plugin`).

### Integration Tests

```shell
mvn verify
```

Integration tests use JUnit 5 `@TestFactory` with dynamic tests. Each scenario is conditionally enabled:

- **local-plain-mailpit** — runs only if Mailpit is reachable on `localhost:1025` (TCP probe with 500ms timeout)
- **external-smtps-conditional** — runs only when SMTP credentials are provided via environment variables or `smtp-integration.properties`

### ByteBuddy Agent

Tests use Mockito's inline mock-maker, which requires the ByteBuddy agent. The CI workflow downloads the agent JAR and passes it via `-javaagent` in `argLine`:

```shell
mvn -DargLine="-javaagent=target/test-agent/byte-buddy-agent-1.17.8.jar" test
```

## Unit Test Patterns

### SMTPConfigTest

Verifies that factory methods produce the correct `SecurityMode` and field values:

```java
SMTPConfig plain = SMTPConfig.forPlain("localhost", 25, "", "");
assertEquals(SMTPConfig.SecurityMode.PLAIN, plain.securityMode);
assertFalse(plain.ssl);

SMTPConfig ssl = SMTPConfig.forSsl("smtp.example.com", 25, "u", "p", 465);
assertEquals(SMTPConfig.SecurityMode.SSL, ssl.securityMode);
assertTrue(ssl.ssl);
assertEquals(465, ssl.sslPort);
```

Also checks that `toString()` contains expected fields (hostname, security mode).

### SendEmailTaskTest

Uses Mockito to mock `HtmlEmail` and inject it via `HtmlEmailFactory`:

```java
HtmlEmail email = mock(HtmlEmail.class);
HtmlEmailFactory factory = () -> email;

SMTPConfig cfg = SMTPConfig.forStartTlsRequired("smtp", 587, "u", "p");
EmailModel model = new EmailModel("a@b", null, "subj", "body");
model.addTo("to@x", "To Name");

SendEmailTask task = new SendEmailTask(cfg, model, "UTF-8", factory);
List<String> errors = task.call();

verify(email).setStartTLSEnabled(true);
verify(email).setStartTLSRequired(true);
verify(email).send();
assertTrue(errors.isEmpty());
```

This pattern avoids needing a live SMTP server while verifying that STARTTLS/SSL configuration is correctly applied to the Commons Email `HtmlEmail` object.

### MainCliTest

Tests CLI flag parsing using picocli's test harness.

## Integration Test Scenarios

### local-plain-mailpit

**Purpose:** Verify end-to-end plain SMTP delivery against a local Mailpit instance.

**Setup:** Start Mailpit (`mailpit` command), which listens on `localhost:1025` by default.

**Behavior:**
- TCP-probes `localhost:1025` — if unreachable, the test is skipped via `Assumptions.assumeTrue`
- Creates `SMTPConfig.forPlain(...)` with dummy credentials
- Sends a test HTML email synchronously
- Asserts the error list is empty

### external-smtps-conditional

**Purpose:** Verify delivery against a real SMTP provider (e.g., Gmail SMTPS) with SSL or STARTTLS.

**Configuration:** Provide credentials via environment variables or `smtp-integration.properties`:

| Variable | Example |
|----------|---------|
| `SMTP_INTEGRATION_HOST` | `smtp.gmail.com` |
| `SMTP_INTEGRATION_PORT` | `465` or `587` |
| `SMTP_INTEGRATION_USERNAME` | `you@gmail.com` |
| `SMTP_INTEGRATION_PASSWORD` | app password |
| `SMTP_INTEGRATION_SENDER` | `you@gmail.com` |
| `SMTP_INTEGRATION_RECIPIENT` | `recipient@example.com` |
| `SMTP_INTEGRATION_STARTTLS` | `true` / `false` (optional) |
| `SMTP_INTEGRATION_STARTTLS_REQUIRED` | `true` / `false` (optional) |

**Security mode selection logic:**
1. Check `SMTP_INTEGRATION_STARTTLS` env var (or properties file)
2. If not set, use port heuristic: port 587 → STARTTLS, otherwise → SSL
3. If STARTTLS, check `SMTP_INTEGRATION_STARTTLS_REQUIRED` for required vs. optional

The test asserts the correct `SecurityMode` on the resulting `SMTPConfig` to prevent silent misconfiguration.

**Debug mode:** Sets `mail.debug=true` system property to capture TLS handshake details in test output.

## CI Configuration

**Source:** `.github/workflows/ci.yml`

- Triggers on pushes that modify `*.java` or `pom.xml` (ignores tags)
- Uses JDK 17 (Temurin)
- Downloads ByteBuddy agent for Mockito inline mock-maker
- Runs unit tests only (`mvn test` — integration tests are excluded by surefire)

**Source:** `.github/workflows/maven-publish.yml`

- Triggers on tag pushes
- Builds the full package including shaded JAR
- Publishes to GitHub Packages
- Uses the same ByteBuddy agent setup

## Writing New Tests

### Adding a Unit Test

1. Create a test class in `src/test/java/com/softinstigate/ermes/mail/`
2. Use JUnit 5 (`@Test`, `@TestFactory`, etc.)
3. For testing `SendEmailTask`, inject a mock `HtmlEmailFactory` via the constructor that accepts it
4. Run with `mvn test`

### Adding an Integration Scenario

1. Add a new `DynamicTest` to `IntegrationScenariosIT.scenarios()`
2. Use `Assumptions.assumeTrue(...)` to skip when prerequisites are missing
3. Follow the existing pattern: probe for availability, create config, send, assert empty error list
4. Run with `mvn verify`

### Mocking Pattern for HtmlEmail

The `HtmlEmailFactory` interface enables dependency injection for testing:

```java
// In production:
new SendEmailTask(config, model);  // uses DefaultHtmlEmailFactory

// In tests:
HtmlEmail mockEmail = mock(HtmlEmail.class);
HtmlEmailFactory mockFactory = () -> mockEmail;
new SendEmailTask(config, model, "UTF-8", mockFactory);
```

This lets you verify `setStartTLSEnabled(true)`, `setSSLOnConnect(true)`, `setFrom(...)`, etc. without network I/O.
