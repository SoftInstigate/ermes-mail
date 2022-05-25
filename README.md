# Ἑρμῆς (Hermês) Mail

[![](https://jitpack.io/v/SoftInstigate/ermes-mail.svg)](https://jitpack.io/#SoftInstigate/ermes-mail)

ErmesMail is a set of Java classes for sending e-mail messages asynchronously, via SMTP servers.

It can be embeded in your Java project as a tiny warapper for the Apache Commons Email library
https://commons.apache.org/proper/commons-email/

Alternatively, it can be used as a command line utility.

It has been developed in Java 17 and built with Maven 3.8.

## Build and execution

1. Build the application with maven: `mvn package`.
2. Start the application by passing the following parameters:

```shell
$ java -jar target/ermes-mail.jar --help

Usage: java -jar ermes-mail.jar [-v] [--help] [--sslon] [-P[=<password>]]
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

## Examples

### Send a test email message to MailHog via cli

To test the sending of e-mails via comand line, we suggest running a local SMTP mock server like [MailHog](https://github.com/mailhog/MailHog). Please look [here](https://github.com/mailhog/MailHog#installation) for MailHogs's installation instructions.

After executing MailHog (usually with the `MailHog` command) you can send your first HTML email message to `localhost` with ErmesEmail:

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

You can read the e-mail message on the [MailHog UI](http://0.0.0.0:8025/).

> **Note**: To send messages via Google SMTP, it is necessary to configure your Gmail account by enabling IMAP. [More information](https://support.google.com/mail/answer/7126229)

## Maven

To use ErmesMail in your maven build, first add the JitPack repository in your pom.xml

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Then add the following dependencies:

```xml
<dependency>
    <groupId>com.github.SoftInstigate</groupId>
    <artifactId>ermes-mail</artifactId>
    <version>1.1.0</version>
    <classifier>shaded</classifier>
</dependency>
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

> **Note**: As ErmesMail depends on `org.apache.commons.commons-email` v1.5, we suggest to include the above runtime dependencies (`javax.mail-api` and `javax.mail`) to prevent classpath conflicts. Specifically, the wrong version of these dependancies may provoke the following runtime exception: `java.lang.NoSuchMethodError: 'void com.sun.mail.util.LineOutputStream.<init>(java.io.OutputStream, boolean)`

### Java example

There are two methods for sending emails: `EmailService.send` is asynchronous and returns a Future list of error strings. the `EmailService.sendSynch` is synchronous and returns a list of error strings.

Below a java fragment:

```java
SMTPConfig smtpConfig = new SMTPConfig("localhost", 1025, "user", "password", false);

EmailModel emailModel = new EmailModel("dick.silly@domain.com", "Dick Silly",
                "Integration Test - " + System.currentTimeMillis(),
                "This is a <strong>HTML</strong> message.");
emailModel.addTo("john.doe@email.com", "John Doe");
emailModel.addTo("serena.wiliams@email.com", "Serena Wiliams");
emailModel.addCc("tom.clancy@email.com", "Tom Clancy");
emailModel.addBcc("ann.smith@email.com", "Ann Smith");

EmailService emailService = new EmailService(smtpConfig, 3);
Future<List<String>> errors = emailService.send(emailModel);

emailService.shutdown();
```
