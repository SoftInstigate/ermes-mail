# README

ErmesMail è un insieme di classi Java per inviare messaggi email tramite server SMTP .

## Build ed esecuzione

1. Build dell'applicazione: `mvn package`
2. Avviare l'applicazione passando i seguenti parametri:

```shell
$ java -jar target/ermes-mail.jar --help

Usage: java -jar ermes-mail.jar [-o] [--help] [-P[=<password>]]
                                -f=<fromAddress> [-h=<smtpHost>] [-l=<sslPort>]
                                -m=<message> [-n=<senderName>] [-p=<smtpPort>]
                                [-r=<recipientName>] -s=<subject>
                                -t=<toAddress> [-u=<user>]
Sends an email to the given recipient.
  -f, --from=<fromAddress>   FROM field
  -h, --host=<smtpHost>      SMTP host
      --help                 display this help message
  -l, --sslport=<sslPort>    SSL port (default is 465)
  -m, --message=<message>    Message body (can be HTML)
  -n, --sender=<senderName>  Sender full name (optional)
  -o, --sslon                Use SSL
  -p, --port=<smtpPort>      SMTP port
  -P, --password[=<password>]
                             SMTP user password
  -r, --recipient=<recipientName>
                             Recipient full name (optional)
  -s, --subject=<subject>    Subject
  -t, --to=<toAddress>       TO field
  -u, --user=<user>          SMTP user name
```

## Esempi

Esempio invio mail a [MailHog](https://github.com/mailhog/MailHog) installato localmente

```shell
$ java -jar target/ermes-mail.jar -h localhost -p 1025 \
  -f sender@email.com -s "test" -m "Prova invio mail <b>HTML</b>." \
  -t receiver@email.com
  
18:54:18.560 [main] INFO com.softinstigate.ermes.mail.EmailService - MailService initialized with SMTPConfig{hostname='localhost', port=1025, username='', password='************', ssl=false, sslPort='465'}
18:54:18.564 [main] INFO com.softinstigate.ermes.mail.EmailService - Sending emails asynchronously...
18:54:18.597 [pool-1-thread-1] INFO com.softinstigate.ermes.mail.SendEmailTask - Processing MailModel{from='sender@email.com', senderFullName='null', subject='test', message='Prova invio mail <b>HTML</b>.', recipients=[Recipient{email='receiver@email.com', name='null'}], attachments=[]}
18:54:18.767 [pool-1-thread-1] INFO com.softinstigate.ermes.mail.SendEmailTask - Email successfully sent to recipients: [receiver@email.com]
18:54:18.768 [main] INFO com.softinstigate.ermes.mail.EmailService - ExecutorService terminated normally after shutdown request.
```


Esempio invio PEC con Legalmail di Infocert:

```shell
$ java -jar target/ermes-mail.jar -h sendm.cert.legalmail.it -o \
  -u M6774147 -P ************ \
  -f maurizio.turatti@legalmail.it -s "test PEC" \
  -m "Prova invio PEC da Legalmail." -t andrea.dicesare@ingpec.eu
```

*NB: Per inviare messaggi tramite SMTP Google è necessario configurare l'account gmail abilitando IMAP. [Maggiori informazioni](https://support.google.com/mail/answer/7126229)*

## Maven

per utilizzare in altri progetti questo JAR con Maven è necessario includere il bucket S3 `maven.softinstigate.com` nel POM:

```xml
<repositories>
    <repository>
        <id>aws-release</id>
        <name>S3 Release Repository</name>
        <url>s3://maven.softinstigate.com/release</url>
    </repository>
    <repository>
        <id>aws-snapshot</id>
        <name>S3 Snapshot Repository</name>
        <url>s3://maven.softinstigate.com/snapshot</url>
    </repository>
</repositories>
```

Inoltre è necessario fare la build passando le credenziali AWS

```shell
mvn -Daws.accessKeyId="$MAVEN_USER" -Daws.secretKey="$MAVEN_PASSWORD"
```

Le variabili `$MAVEN_USER` e `MAVEN_PASSWORD` sono le chiavi di accesso dell'utente `maven` nell'account AWS di SoftInstigate.
