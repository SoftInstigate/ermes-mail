# Ἑρμῆς (Hermês) Mail

[![JitPack version](https://jitpack.io/v/com.softinstigate/ermes-mail.svg)](https://jitpack.io/#com.softinstigate/ermes-mail)

ErmesMail is a set of Java classes for sending e-mail messages asynchronously, via SMTP servers.

1. It can be embedded in your Java project as a tiny wrapper for the [Apache Commons Email library](https://commons.apache.org/proper/commons-email/).
2. It can be used as a handy command line utility, to send emails programmatically from the shell.

ErmesMail is developed in Java 17 and built with Maven 3.8.

## JavaDocs

JavaDocs are available at the [ErmesMail JavaDocs](https://jitpack.io/com/github/softinstigate/ermes-mail/latest/javadoc/).

## Build and cli execution

1. Build the application with maven: `mvn package`.
2. Run the Java executable by passing the following parameters:

```shell
$ java -jar target/ermes-mail.jar --help

Usage: java -jar ermes-mail.jar [-v] [--help] [--sslon] [--starttls] [--starttls-required] [-P[=<password>]]
                                -b=<message> -f=<fromAddress> [-h=<smtpHost>]
                                [-n=<senderName>] [-p=<smtpPort>] -s=<subject>
                                [--sslport=<sslPort>] [-u=<user>]
                                [--bcc=<bccList>[,<bccList>...]...]...
                                [--cc=<ccList>[,<ccList>...]...]...
                                --to=<toList>[,<toList>...]... [--to=<toList>[,
                                <toList>...]...]...
Sends an HTML email to the given recipient(s).
  -h, --host=<smtpHost>      SMTP host.
  -p, --port=<smtpPort>      SMTP port.
  -u, --user=<user>          SMTP user name.
  -P, --password[=<password>]
                             SMTP user password.
    --sslon                Use SSL.
    --sslport=<sslPort>    SSL port (default is 465).
    --starttls             Enable STARTTLS (upgrade to TLS if server supports it).
    --starttls-required    Require STARTTLS (fail if not supported).
  -f, --from=<fromAddress>   FROM field.
  -n, --sender=<senderName>  Sender full name (optional).
  -s, --subject=<subject>    Subject.
  -b, --body=<message>       Message body (can be HTML).
      --to=<toList>[,<toList>...]...
                             List of mandatory TO recipients.
      --cc=<ccList>[,<ccList>...]...
                             List of optional CC recipients.
      --bcc=<bccList>[,<bccList>...]...
                             List of optional BCC recipients.
      --help                 display this help message.
  -v, --version              print version information and exit.
Copyright(c) 2022 SoftInstigate srl (https://www.softinstigate.com)
```

## Send a test email message to Mailpit

To test the sending of e-mails via command line, we suggest running a local SMTP mock server like [Mailpit](https://github.com/axllent/mailpit). Please look at the [Mailpit installation instructions](https://github.com/axllent/mailpit#installation) for setup details.

After executing Mailpit (usually with the `mailpit` command) you can send your first HTML email message to `localhost` with ErmesMail:

```shell
$ java -jar target/ermes-mail.jar -h localhost -p 1025 \
  -f sender@email.com -s "test" -b "This is a <strong>HTML</strong> test email." \
  --to receiver@email.com
  
mag 24, 2022 4:46:16 PM com.softinstigate.ermes.mail.EmailService <init>
INFORMAZIONI: MailService initialized with SMTPConfig{hostname='localhost', port=1025, username='', ssl=false, sslPort=465}
mag 24, 2022 4:46:16 PM com.softinstigate.ermes.mail.EmailService send
INFORMAZIONI: Sending emails asynchronously...
mag 24, 2022 4:46:16 PM com.softinstigate.ermes.mail.SendEmailTask call
INFORMAZIONI: Processing MailModel{from='sender@email.com', senderFullName='null', subject='test', message='This is a <strong>HTML</strong> test email.', to=[Recipient{email='receiver@email.com', name='null'}], cc=[], bcc=[], attachments=[]}
mag 24, 2022 4:46:16 PM com.softinstigate.ermes.mail.SendEmailTask call
INFORMAZIONI: Email successfully sent!
TO: [Recipient{email='receiver@email.com', name='null'}]
CC: []
BCC: []
mag 24, 2022 4:46:16 PM com.softinstigate.ermes.mail.EmailService shutdown
INFORMAZIONI: ExecutorService terminated normally after shutdown request.```
```

You can read the e-mail message on the [Mailpit UI](http://0.0.0.0:8025/).

> **Note**: To send messages via Google SMTP, it is necessary to configure your Gmail account by enabling IMAP. [More information](https://support.google.com/mail/answer/7126229)

## Add ErmesMail to your Maven project

To use ErmesMail in your Maven build, first add the JitPack repository in your pom.xml

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Then add the following dependency:

```xml
<dependency>
    <groupId>com.softinstigate</groupId>
    <artifactId>ermes-mail</artifactId>
    <version>2.0.0</version>
    <classifier>shaded</classifier>
</dependency>
```

> **Warning**: As ErmesMail depends on `org.apache.commons.commons-email` v1.5, we suggest including the below runtime dependencies (`javax.mail-api` and `javax.mail`) to prevent classpath conflicts. The wrong version of these dependencies, included by other libraries, might provoke the following runtime exception when sending emails: `java.lang.NoSuchMethodError: 'void com.sun.mail.util.LineOutputStream.<init>(java.io.OutputStream, boolean)`

```xml
<dependency>
    <groupId>javax.mail</groupId>
    <artifactId>javax.mail-api</artifactId>
    <version>1.5.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>javax.mail</artifactId>
    <version>1.5.6</version>
    <scope>runtime</scope>
</dependency>
```

You can run `mvn dependency:tree` in your project to check if other artifacts are including these.

### Java example

There are two methods for sending emails: `EmailService.send` is asynchronous and returns a Future list of error strings. The `EmailService.sendSynch` is synchronous and returns a list of error strings. If the list is empty, it means no errors. However, the [`SendEmailTask`](src/main/java/com/softinstigate/ermes/mail/SendEmailTask.java) logs exceptions anyway.

You may want to use the asynchronous invocation only in case you have to send many emails in parallel and don't want to block the rest of the program; otherwise, the synchronous method works just fine.

Internally the [`EmailService`](src/main/java/com/softinstigate/ermes/mail/EmailService.java) uses a [java.util.concurrent.ExecutorService](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html) to send emails in parallel.

A good understanding of [Java Futures](https://www.baeldung.com/java-future) would help you implement the best waiting strategy.

Below a java fragment, for example:

```java
// Use the factory methods on SMTPConfig to express the desired security mode.
SMTPConfig smtpConfig = SMTPConfig.forPlain("localhost", 1025, "user", "password");

EmailModel emailModel = new EmailModel(
    "dick.silly@domain.com", "Dick Silly",
    "Test email - " + System.currentTimeMillis(),
    "This is a <strong>HTML</strong> message.");
emailModel.addTo("john.doe@email.com", "John Doe");
emailModel.addTo("serena.wiliams@email.com", "Serena Wiliams");
emailModel.addCc("tom.clancy@email.com", "Tom Clancy");
emailModel.addBcc("ann.smith@email.com", "Ann Smith");


EmailService emailService = new EmailService(smtpConfig, 3); // 3 threads pool
Future<List<String>> errors = emailService.send(emailModel); // send is async


emailService.shutdown();


List<String> listOfErrors = errors.get(); // WARNING: Future.get() is blocking
if (!listOfErrors.isEmpty()) {
    System.err.println("Errors sending emails: " + listOfErrors.toString());
}
```

## SMTP Configuration and Testing

ErmesMail supports plain SMTP, SSL (SMTPS), and STARTTLS (opportunistic or required).

You can configure the SMTP host, port, user, password, and security mode via the command line or programmatically using the `SMTPConfig` factory methods.

To test different SMTP configurations:

1. Use a local SMTP server like Mailpit for development and testing.
2. For SSL (implicit TLS), use the `--sslon` and `--sslport` options (default SSL port is 465).
3. For STARTTLS, use `--starttls` to enable opportunistic STARTTLS, and add `--starttls-required` if you want the client to fail when the server does not advertise STARTTLS.
4. For plain SMTP, omit the `--sslon` and `--starttls` flags and use the standard port (usually 25 or 1025 for local testing).
5. To test with real SMTP providers (e.g., Gmail), ensure your credentials and security settings are correct and that your network/firewall allows outbound connections to the SMTP server and port.

If you encounter issues, check the logs for detailed error messages. For troubleshooting tips, see the [project issues](https://github.com/SoftInstigate/ermes-mail/issues) or contact the maintainers.

## Running integration tests

Integration tests are consolidated in `IntegrationScenariosIT` and cover two scenarios:

- `local-plain-mailpit`: sends plain SMTP to a local Mailpit instance (localhost:1025). This test is executed only when Mailpit is reachable on `localhost:1025` (the test probes the TCP port and will be skipped automatically if nothing is listening).
- `external-smtps-conditional`: performs an implicit SSL (SMTPS) send against an external SMTP provider and is run only when integration credentials/configuration are provided via environment variables or a local properties file.

Provide external SMTP configuration either using environment variables or a `smtp-integration.properties` file in the project root with these keys:

- `SMTP_INTEGRATION_HOST` (e.g. `smtps.example.com`)
- `SMTP_INTEGRATION_PORT` (e.g. `465`)
- `SMTP_INTEGRATION_USERNAME`
- `SMTP_INTEGRATION_PASSWORD`
- `SMTP_INTEGRATION_SENDER`
- `SMTP_INTEGRATION_RECIPIENT`
- `SMTP_INTEGRATION_SSLPORT` (optional, defaults to the port above)

Example `smtp-integration.properties` (do not commit this file):

```properties
SMTP_INTEGRATION_HOST=smtps.example.com
SMTP_INTEGRATION_PORT=465
SMTP_INTEGRATION_USERNAME=info@yourdomain.example
SMTP_INTEGRATION_PASSWORD=supersecret
SMTP_INTEGRATION_SENDER=info@yourdomain.example
SMTP_INTEGRATION_RECIPIENT=you@example.com
SMTP_INTEGRATION_SSLPORT=465
```

Run the tests (integration tests are executed by the Maven `verify` phase). Use JavaMail debug to capture client-side TLS/SSL handshake logs when the external scenario runs:

```bash
mvn -Dmail.debug=true -DfailIfNoTests=false verify
```

To run only integration tests while skipping unit tests:

```bash
mvn -Dmail.debug=true -DskipTests=true -DfailIfNoTests=false verify
```

Notes:

- The local Mailpit scenario will be automatically skipped if nothing is listening on `localhost:1025`.
- The external SMTPS scenario is conditional and will be skipped when the required configuration is not present (either env vars or `smtp-integration.properties` / `.env`).
- Older individual integration test files have been removed; `IntegrationScenariosIT` is the canonical integration test.
- Keep integration credentials out of the repository; `smtp-integration.properties` is ignored by `.gitignore` and an example file is provided.

## Migration note

Version 2.0 introduces a breaking change: the boolean-heavy `SMTPConfig` constructors were removed in favor of explicit factory methods that make the security policy clear.

Old code (pre-2.0):

```java
// Older constructor with boolean flags (removed in 2.0)
SMTPConfig smtpConfig = new SMTPConfig("localhost", 1025, "user", "password", false /*ssl*/);
```

New code (2.0+):

```java
// Use factory methods to express intent clearly
SMTPConfig smtpPlain = SMTPConfig.forPlain("localhost", 1025, "user", "password");
SMTPConfig smtpSsl = SMTPConfig.forSsl("smtp.example.com", 465, "user", "password", 465);
SMTPConfig smtpStartTls = SMTPConfig.forStartTlsOptional("smtp.example.com", 587, "user", "password");
SMTPConfig smtpStartTlsRequired = SMTPConfig.forStartTlsRequired("smtp.example.com", 587, "user", "password");
```

This makes it explicit whether you want plain SMTP, implicit SSL (SMTPS), or STARTTLS (optional or required).
