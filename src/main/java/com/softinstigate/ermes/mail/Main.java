package com.softinstigate.ermes.mail;

import org.apache.commons.mail.EmailException;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "java -jar ermes-mail.jar", description = "Sends an email to the given recipient.")
public class Main implements Callable<Integer> {

    @CommandLine.Option(names = {"-h", "--host"}, defaultValue = "localhost", description = "SMTP host")
    private String smtpHost;

    @CommandLine.Option(names = {"-p", "--port"}, defaultValue = "25", description = "SMTP port")
    private int smtpPort;

    @CommandLine.Option(names = {"-u", "--user"}, defaultValue = "", description = "SMTP user name")
    private String user;

    @CommandLine.Option(names = {"-P", "--password"}, defaultValue = "", arity = "0..1", interactive = true, description = "SMTP user password")
    private String password;

    @CommandLine.Option(names = {"-o", "--sslon"}, defaultValue = "false", description = "Use SSL")
    private boolean sslOn;

    @CommandLine.Option(names = {"-l", "--sslport"}, defaultValue = SMTPConfig.DEFAULT_SSL_PORT, description = "SSL port (default is 465)")
    private String sslPort;

    @CommandLine.Option(names = {"-f", "--from"}, required = true, description = "FROM field")
    private String fromAddress;

    @CommandLine.Option(names = {"-n", "--sender"}, description = "Sender full name (optional)")
    private String senderName;

    @CommandLine.Option(names = {"-s", "--subject"}, required = true, description = "Subject")
    private String subject;

    @CommandLine.Option(names = {"-m", "--message"}, required = true, description = "Message body (can be HTML)")
    private String message;

    @CommandLine.Option(names = {"-t", "--to"}, required = true, description = "TO field")
    private String toAddress;

    @CommandLine.Option(names = {"-r", "--recipient"}, description = "Recipient full name (optional)")
    private String recipientName;

    @CommandLine.Option(names = {"--help"}, usageHelp = true, description = "display this help message")
    private boolean help;

    public Integer call() {
        SMTPConfig smtpConfig = new SMTPConfig(
                smtpHost,
                smtpPort,
                user,
                password,
                sslOn,
                sslPort);
        EmailModel emailModel = new EmailModel(
                fromAddress,
                senderName,
                subject,
                message);
        emailModel.addRecipient(toAddress, recipientName);

        EmailService emailService = new EmailService(smtpConfig, 1);
        emailService.send(emailModel);
        emailService.shutdown();
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
