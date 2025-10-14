# GitHub Copilot Instructions for ErmesMail

## Project Overview
ErmesMail is a Java 17 email library that wraps Apache Commons Email for both programmatic use and CLI execution. It provides asynchronous email sending with explicit SMTP security configuration patterns.

## Architecture & Core Components

### Security-First SMTP Configuration (`SMTPConfig.java`)
- **Never use constructors directly** - use factory methods that make security intent explicit:
  - `SMTPConfig.forPlain()` - Plain SMTP (no encryption)
  - `SMTPConfig.forSsl()` - Implicit SSL/SMTPS (port 465)
  - `SMTPConfig.forStartTlsOptional()` - Opportunistic STARTTLS
  - `SMTPConfig.forStartTlsRequired()` - Mandatory STARTTLS
- Migration from v1.x: Replace boolean-heavy constructors with factory methods

### Asynchronous Email Service (`EmailService.java`)
- Uses `ExecutorService` with configurable thread pool for parallel email sending
- Two sending patterns:
  - `send()` - Returns `Future<List<String>>` for async processing
  - `sendSynch()` - Blocks and returns `List<String>` directly
- Always call `shutdown()` to terminate executor cleanly
- Empty error list = success; non-empty = check `SendEmailTask` logs

### Email Model Pattern (`EmailModel.java`)
- Immutable sender/subject/message fields with mutable recipient collections
- Use `addTo()`, `addCc()`, `addBcc()` methods to build recipient lists
- Supports file attachments via `Attachment` class

## Key Development Workflows

### Building & CLI Testing
```bash
mvn package                                    # Creates target/ermes-mail.jar
java -jar target/ermes-mail.jar --help        # Show CLI options
```

### Local Development with Mailpit
```bash
# Start Mailpit for testing (separate terminal)
mailpit

# Send test email
java -jar target/ermes-mail.jar -h localhost -p 1025 \
  -f sender@test.com -s "Test" -b "HTML <strong>content</strong>" \
  --to recipient@test.com
```

### Integration Testing Strategy
- **Conditional tests**: `IntegrationScenariosIT.java` auto-detects available scenarios
- **Local scenario**: Runs only if Mailpit is reachable on `localhost:1025`
- **External SMTPS**: Requires `smtp-integration.properties` or env vars
- Use `mvn verify` (not `test`) to run integration tests
- Debug TLS issues: `mvn -Dmail.debug=true verify`

### Dependency Management Patterns
- **Jakarta Mail transition**: Uses `jakarta.mail` 1.6.7+ (not legacy `javax.mail`)
- **Commons Email**: Version 1.6.0+ for Jakarta Mail compatibility
- **JavaMail version conflicts**: If you see `NoSuchMethodError` for `LineOutputStream`, check dependency tree for older javax.mail versions

## Project-Specific Conventions

### Error Handling Philosophy
- `SendEmailTask` logs all exceptions automatically via `java.util.logging`
- Return empty error lists for success, populated lists for failures
- Use `Future.get()` carefully - it blocks until completion

### Secure Logging Patterns
- **Always use `toSecureString()`** for logging `EmailModel` and `SMTPConfig` objects
- **Never log email content** - use `model.toSecureString()` instead of `model.toString()`
- **Redact sensitive information** - usernames, message content, and error details
- Pattern: Log metadata (counts, lengths) instead of actual content

### Security Configuration Patterns
```java
// ✅ Explicit security intent (v2.0+)
SMTPConfig config = SMTPConfig.forStartTlsRequired("smtp.example.com", 587, "user", "pass");

// ❌ Avoid - removed in v2.0
SMTPConfig config = new SMTPConfig("host", 587, "user", "pass", false);
```

### CLI Integration Points
- `Main.java` uses PicoCLI for command parsing
- Supports both individual recipients and comma-separated lists
- Environment variable precedence over direct CLI args for sensitive data

### Test Configuration Requirements
- Keep `smtp-integration.properties` out of git (in `.gitignore`)
- Use `.env` or environment variables for CI/CD integration
- Integration tests gracefully skip when configuration unavailable

## Common Pitfalls & Solutions

### JavaMail Version Conflicts
- **Symptom**: `NoSuchMethodError: LineOutputStream.<init>`
- **Solution**: Ensure Jakarta Mail 1.6.7+ is used, not legacy javax.mail
- **Check**: `mvn dependency:tree | grep mail` should show `jakarta.mail`

### Executor Shutdown
- Always call `EmailService.shutdown()` or use try-with-resources pattern
- Executor won't terminate JVM automatically without proper shutdown

### SMTP Security Testing
- Test all four security modes with appropriate ports (25/587/465)
- Use Mailpit for local development, real SMTP for production validation
- AWS SES/Gmail require specific authentication and security configurations

### Security Considerations
- **Logging**: Use `toSecureString()` methods to avoid exposing sensitive data in logs
- **Error handling**: Be careful with exception messages that may contain credentials
- **Configuration**: Never commit `smtp-integration.properties` with real credentials