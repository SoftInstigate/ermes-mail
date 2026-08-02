# Ἑρμῆς (Hermês) Mail

[![JitPack version](https://jitpack.io/v/com.softinstigate/ermes-mail.svg)](https://jitpack.io/#com.softinstigate/ermes-mail)

ErmesMail is a Java 17 library and CLI tool for sending HTML emails via SMTP. It wraps [Apache Commons Email](https://commons.apache.org/proper/commons-email/) with a clean API and adds a [picocli](https://picocli.info/)-based command-line interface for shell usage.

- **As a library** — embed in your Maven project and call `EmailService` programmatically.
- **As a CLI tool** — build a fat JAR and send emails from the shell.

## JavaDocs

JavaDocs are available at [jitpack.io](https://jitpack.io/com/github/softinstigate/ermes-mail/latest/javadoc/).

## OpenWiki Documentation

- [Quickstart](openwiki/quickstart.md) — build, run, and first steps
- [Architecture Overview](openwiki/architecture/overview.md) — package structure, class relationships
- [Domain Concepts](openwiki/domain/concepts.md) — SMTPConfig, EmailModel, EmailService, SecurityMode
- [Source Map](openwiki/source-map.md) — file-by-file guide
- [Testing Guide](openwiki/testing/guide.md) — unit tests, integration tests, mocking patterns
- [Operations Runbook](openwiki/operations/runbook.md) — SMTP configuration, CI/CD, troubleshooting

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

| Flag | Description |
|------|-------------|
| `-h, --host` | SMTP host (default: `localhost`) |
| `-p, --port` | SMTP port (default: `25`) |
| `-u, --user` / `-P, --password` | Credentials |
| `--sslon` + `--sslport` | Implicit SSL (SMTPS, typically port 465) |
| `--starttls` / `--starttls-required` | STARTTLS upgrade |
| `-f, --from` / `-n, --sender` | Sender address and optional display name |
| `-s, --subject` / `-b, --body` | Email subject and HTML body |
| `--to`, `--cc`, `--bcc` | Recipients (comma-separated) |

### Quick test with Mailpit

```shell
# Start Mailpit (local SMTP mock), then:
java -jar target/ermes-mail.jar -h localhost -p 1025 \
  -f sender@email.com -s "Test" -b "<strong>Hello</strong>" \
  --to receiver@email.com
```

Read the message on the [Mailpit UI](http://0.0.0.0:8025/).

## Library Usage

### Maven setup

Add the [JitPack repository](https://jitpack.io/#SoftInstigate/ermes-mail) to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Then add the dependency:

```xml
<dependency>
    <groupId>com.softinstigate</groupId>
    <artifactId>ermes-mail</artifactId>
    <version>3.0.0</version>
    <classifier>shaded</classifier>
</dependency>
```

> **Dependency note**: ErmesMail depends on `org.apache.commons:commons-email:1.6.0`, which brings `com.sun.mail:jakarta.mail:1.6.7` and `com.sun.activation:jakarta.activation:1.2.1` transitively. If your application has other mail-related dependencies, verify your effective classpath with `mvn dependency:tree` and align mail/activation versions to avoid runtime conflicts.

### Basic example

```java
SMTPConfig smtpConfig = SMTPConfig.forPlain("localhost", 1025, "user", "password");

EmailModel emailModel = new EmailModel(
    "sender@domain.com", "Sender Name",
    "Test email - " + System.currentTimeMillis(),
    "This is a <strong>HTML</strong> message.");
emailModel.addTo("recipient@email.com", "Recipient Name");
emailModel.addCc("cc@email.com", "CC Name");
emailModel.addBcc("bcc@email.com", "BCC Name");

try (EmailService emailService = new EmailService(smtpConfig)) {
    Future<List<String>> errors = emailService.send(emailModel);
    List<String> listOfErrors = errors.get(); // blocking
    if (!listOfErrors.isEmpty()) {
        System.err.println("Errors: " + listOfErrors);
    }
}
```

`EmailService` implements `AutoCloseable`, so the thread pool (if created) is shut down automatically.

### Synchronous sending

```java
try (EmailService emailService = new EmailService(smtpConfig)) {
    List<String> errors = emailService.sendSynch(emailModel);
    if (!errors.isEmpty()) {
        System.err.println("Errors: " + errors);
    }
}
```

### Virtual threads support

If your application manages concurrency externally (e.g. with virtual threads), you can initialize `EmailService` without an internal thread pool:

```java
// No thread pool — send() falls back to synchronous execution
EmailService emailService = new EmailService(smtpConfig, 0);

// Both methods work, but send() blocks since there is no pool
emailService.send(emailModel);      // blocks, returns completed Future
emailService.sendSynch(emailModel); // blocks, returns error list
```

When `threadPoolSize` is `0`:
- No `ExecutorService` is ever created.
- `send()` executes synchronously and returns an already-completed `Future`.
- `sendSynch()` works as usual.
- `shutdown()` is a no-op.

This is useful in frameworks like RestHeart that use virtual threads — the framework manages concurrency, and ErmesMail does not duplicate it with an internal pool.

### SMTP security modes

`SMTPConfig` uses factory methods to express the security intent:

```java
SMTPConfig plain     = SMTPConfig.forPlain("localhost", 1025, "user", "password");
SMTPConfig ssl       = SMTPConfig.forSsl("smtp.example.com", 465, "user", "password", 465);
SMTPConfig startTls  = SMTPConfig.forStartTlsOptional("smtp.example.com", 587, "user", "password");
SMTPConfig startTlsR = SMTPConfig.forStartTlsRequired("smtp.example.com", 587, "user", "password");
```

`--sslon` and `--starttls` are mutually exclusive.

### Socket timeouts

`SMTPConfig` includes configurable connection and socket timeouts (defaults: 10s connection, 60s socket). These are applied automatically via `HtmlEmail.setSocketConnectionTimeout()` and `HtmlEmail.setSocketTimeout()`.

## Running integration tests

Integration tests are in `IntegrationScenariosIT` and cover two scenarios:

- **`local-plain-mailpit`** — sends plain SMTP to a local Mailpit instance (`localhost:1025`). Skipped automatically if Mailpit is not reachable.
- **`external-smtps-conditional`** — sends via an external SMTP provider. Only runs when credentials are provided via environment variables or a local properties file.

Provide external SMTP configuration via environment variables or a `smtp-integration.properties` file in the project root:

```properties
SMTP_INTEGRATION_HOST=smtps.example.com
SMTP_INTEGRATION_PORT=465
SMTP_INTEGRATION_USERNAME=info@yourdomain.example
SMTP_INTEGRATION_PASSWORD=supersecret
SMTP_INTEGRATION_SENDER=info@yourdomain.example
SMTP_INTEGRATION_RECIPIENT=you@example.com
```

Run integration tests:

```bash
mvn verify
```

With JavaMail debug output:

```bash
mvn -Dmail.debug=true verify
```

## Migration from 2.x to 3.0

Version 3.0 includes breaking changes:

| Change | Impact |
|--------|--------|
| `Recipient` and `Attachment` are now **records** | `recipient.email` → `recipient.email()`, `recipient.name` → `recipient.name()`, etc. |
| Input validation on constructors | `null` values for `from`, `subject`, `message`, `email`, `url` now throw immediately |
| `EmailModel.toString()` | Changed from `"MailModel{"` to `"EmailModel{"` |
| `EmailService.send()` after shutdown | Throws `IllegalStateException` instead of `RejectedExecutionException` |

New features in 3.0:

| Feature | Description |
|---------|-------------|
| Lazy thread pool | `ExecutorService` created only on first `send()` call |
| `threadPoolSize = 0` | No internal pool; `send()` falls back to synchronous execution |
| `AutoCloseable` | `EmailService` can be used with try-with-resources |
| Socket timeouts | Configurable `connectionTimeout` and `socketTimeout` in `SMTPConfig` |
| Input validation | Early `NullPointerException` / `IllegalArgumentException` on invalid inputs |
| Comprehensive test suite | 74 unit tests covering all classes and error paths |

### 2.0 migration (from pre-2.0)

The boolean-heavy `SMTPConfig` constructors were removed in 2.0 in favor of explicit factory methods:

```java
// Pre-2.0 (removed)
SMTPConfig cfg = new SMTPConfig("localhost", 1025, "user", "password", false);

// 2.0+ (current)
SMTPConfig cfg = SMTPConfig.forPlain("localhost", 1025, "user", "password");
```

## License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

Copyright(c) SoftInstigate srl (https://www.softinstigate.com)
