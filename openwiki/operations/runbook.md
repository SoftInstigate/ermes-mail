---
type: Operations
title: ErmesMail Operations & Runbook
description: Operational guidance for ErmesMail — SMTP configuration, CI/CD pipelines, publishing, troubleshooting common issues.
tags: [operations, smtp, ci, cd, troubleshooting, maven]
---

# Operations & Runbook

## Build & Package

### Standard Build

```shell
mvn clean package
```

Produces:
- `target/ermes-mail-<version>.jar` — standard JAR
- `target/ermes-mail.jar` — shaded fat JAR (all dependencies bundled)

The shaded JAR is the primary artifact for CLI usage and distribution.

### Clean Build (Skip License Headers)

```shell
mvn clean package -DskipUpdateLicense=true
```

The `license-maven-plugin` updates Apache 2.0 headers in source files. Set `skipUpdateLicense=true` (default in pom.xml) to skip this during normal builds.

## SMTP Configuration

### Security Modes

| Mode | Typical Port | CLI Flag | Factory Method |
|------|--------------|----------|----------------|
| Plain SMTP | 25, 1025 | (none) | `SMTPConfig.forPlain(...)` |
| SSL (SMTPS) | 465 | `--sslon --sslport 465` | `SMTPConfig.forSsl(...)` |
| STARTTLS (optional) | 587 | `--starttls` | `SMTPConfig.forStartTlsOptional(...)` |
| STARTTLS (required) | 587 | `--starttls --starttls-required` | `SMTPConfig.forStartTlsRequired(...)` |

### Common SMTP Providers

**Gmail:**
- Host: `smtp.gmail.com`
- Port: 587 (STARTTLS) or 465 (SSL)
- Requires: App Password (2FA must be enabled), IMAP enabled in Gmail settings
- CLI: `--starttls` with port 587

**Mailpit (local dev):**
- Host: `localhost`
- Port: 1025
- No auth required
- CLI: no security flags

### Integration Test Configuration

Create `smtp-integration.properties` in the project root (git-ignored):

```properties
SMTP_INTEGRATION_HOST=smtp.gmail.com
SMTP_INTEGRATION_PORT=587
SMTP_INTEGRATION_USERNAME=you@gmail.com
SMTP_INTEGRATION_PASSWORD=your-app-password
SMTP_INTEGRATION_SENDER=you@gmail.com
SMTP_INTEGRATION_RECIPIENT=recipient@example.com
# Optional:
# SMTP_INTEGRATION_STARTTLS=true
# SMTP_INTEGRATION_STARTTLS_REQUIRED=false
```

See `smtp-integration.properties.example` for the template. Alternatively, set environment variables with the same names.

## CI/CD Pipelines

### CI Workflow (`.github/workflows/ci.yml`)

**Trigger:** Push to any branch when `*.java` or `pom.xml` changes (ignores tags).

**Steps:**
1. Checkout code
2. Cache Maven repository
3. Set up JDK 17 (Temurin)
4. Download ByteBuddy agent JAR (version 1.17.5 in CI, 1.17.8 in publish)
5. Run unit tests with `-javaagent` for Mockito inline mock-maker

**Note:** Integration tests (`*IT.java`) are excluded by surefire configuration — they only run via `mvn verify` locally.

### Maven Publish Workflow (`.github/workflows/maven-publish.yml`)

**Trigger:** Push of any tag.

**Steps:**
1. Checkout, cache, JDK 17 setup
2. Download ByteBuddy agent
3. Build with `mvn package`
4. Publish to GitHub Packages with `mvn deploy` (requires `GITHUB_TOKEN`)

**Permissions:** `contents: read`, `packages: write`.

### OpenWiki Update Workflow (`.github/workflows/openwiki-update.yml`)

Automates documentation refresh. Not part of the build pipeline.

## Dependency Management

### Key Dependencies

| Dependency | Version | Scope | Purpose |
|------------|---------|-------|---------|
| `org.apache.commons:commons-email` | 1.6.0 | compile | Email sending via HtmlEmail |
| `info.picocli:picocli` | 4.7.7 | compile | CLI argument parsing |
| `javax.mail:javax.mail-api` | 1.6.2 | runtime | JavaMail API (pre-Jakarta) |
| `com.sun.mail:javax.mail` | 1.6.2 | runtime | JavaMail implementation |
| `org.junit.jupiter:junit-jupiter` | 5.12.2 | test | Test framework |
| `org.mockito:mockito-core` | 5.18.0 | test | Mocking |
| `net.bytebuddy:byte-buddy-agent` | 1.17.8 | test | Mockito inline mock-maker agent |

### javax.mail Version Warning

Commons Email 1.6.0 requires the pre-Jakarta `javax.mail` namespace. If your project includes other libraries that bring in a different JavaMail version, you may see:

```
java.lang.NoSuchMethodError: 'void com.sun.mail.util.LineOutputStream.<init>(java.io.OutputStream, boolean)'
```

**Fix:** Add explicit runtime dependencies for `javax.mail-api:1.6.2` and `com.sun.mail:javax.mail:1.6.2` in your pom.xml. Run `mvn dependency:tree` to identify conflicts.

### ByteBuddy Version Sync

The `bytebuddy.version` property in pom.xml must stay in sync with the Mockito version. If you see warnings about ByteBuddy incompatibility at test startup, update the property to match Mockito's expected ByteBuddy version.

## Distribution

### JitPack

ErmesMail is distributed via [JitPack](https://jitpack.io/#SoftInstigate/ermes-mail). Consumers add the JitPack repository and the dependency:

```xml
<dependency>
    <groupId>com.softinstigate</groupId>
    <artifactId>ermes-mail</artifactId>
    <version>2.1.0</version>
    <classifier>shaded</classifier>
</dependency>
```

### GitHub Packages

The `maven-publish.yml` workflow publishes to GitHub Packages on tag push. Requires a GitHub token with `packages:write` permission.

## Troubleshooting

### Emails not sending

1. Check SMTP host/port — use `mail.debug=true` system property for verbose JavaMail output
2. Verify credentials — `SMTPConfig.toSecureString()` confirms `hasCredentials=true`
3. Check firewall — outbound connections to SMTP port must be allowed
4. For Gmail: ensure IMAP is enabled and you're using an App Password (not your account password)

### STARTTLS connection refused

- Port 587 is the standard STARTTLS submission port
- Some providers block STARTTLS on port 25
- Use `--starttls-required` to fail fast if the server doesn't advertise STARTTLS

### SSL handshake failures

- Verify the SSL port (465 is standard for SMTPS)
- Check that the server's certificate is trusted by your JVM's truststore
- Use `mail.debug=true` to see the TLS handshake details

### javax.activation.UnsupportedDataTypeException

`SendEmailTask` includes a workaround for this: it registers explicit `MailcapCommandMap` entries for common MIME types and sets the thread's context classloader. If you still see this error in a complex classloader environment (e.g., application server), you may need to register the same entries in your code.

### Thread pool exhaustion

If `EmailService.shutdown()` logs "timeout elapsed: some emails may not have been sent", increase the shutdown timeout via `shutdown(long timeout)` or increase the thread pool size.

## Version History

See [CHANGELOG.md](../../CHANGELOG.md) for detailed release notes. Key milestones:

- **1.1.0** (2022-05-25) — Initial public release
- **2.0.0** (2025-09-08) — Breaking API: factory methods for SMTPConfig, STARTTLS support, HtmlEmailFactory for testability
- **2.1.0** (2025-10-14) — Security hardening: redacted logging, STARTTLS env var config for integration tests
