# README

ErmesMail è un insieme di classi Java per inviare messaggi email tramite server SMTP .

### SimpleMessage

1. Build dell'applicazione: `mvn install`
2. Avviare l'applicazione passando i seguenti parametri:

`java -jar ermes-mail.jar <from-email> <to-email> <mail-object> <mail-message> <smtp-hostname> <smtp-port> <username> <password>`


*NB: Per inviare messaggi tramite SMTP Google è necessario configurare l'account gmail abilitando IMAP. [Maggiori informazioni](https://support.google.com/mail/answer/7126229)*

### Troubleshooting

`javax.mail.AuthenticationFailedException: 535-5.7.8 Username and Password not accepted.` [Soluzione] (https://stackoverflow.com/questions/43406528/javamail-api-username-and-password-not-accepted-gmail)

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
