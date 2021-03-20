package com.softinstigate.ermes.mail;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.*;

/**
 * Receive a SMTP server configuration and send emails with HTML content
 */
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
    private static final long DEFAULT_SEND_TIMEOUT = 5; // timeout in seconds

    private final HtmlEmail email = new HtmlEmail();

    /**
     * Constructor
     *
     * @param smtpConfig the SMTP server credentials and configuration
     */
    public EmailService(SMTPConfig smtpConfig) {
        email.setHostName(smtpConfig.hostname);
        email.setSmtpPort(smtpConfig.port);
        email.setAuthentication(smtpConfig.username, smtpConfig.password);
        email.setSSLOnConnect(smtpConfig.ssl);
        email.setSslSmtpPort(smtpConfig.sslPort);

        LOGGER.info("MailService initialized.");
        LOGGER.debug(smtpConfig.toString());
    }

    /**
     * Send an email with timeout set to DEFAULT_SEND_TIMEOUT
     *
     * @param model the email object to send
     * @throws EmailException in case of SMTP errors
     */
    public void send(EmailModel model) throws EmailException {
        send(model, DEFAULT_SEND_TIMEOUT);
    }

    /**
     * Send an email with explicit timeout
     *
     * @param model   the email object to send
     * @param timeout max seconds to wait for all threads to shutdown
     * @throws EmailException in case of SMTP errors
     */
    public void send(EmailModel model, long timeout) throws EmailException {
        LOGGER.debug("Email to send: {}", model.toString());
        email.setFrom(model.from, model.senderFullName);
        email.setSubject(model.subject);
        email.setMsg(model.message);
        ExecutorService executor = Executors.newFixedThreadPool(model.getRecipients().size());
        for (EmailModel.Recipient recipient : model.getRecipients()) {
            try {
                this.email.addTo(recipient.email, recipient.name);
                processAttachments(model);
                executor.execute(new SendEmailTask(email));
            } catch (EmailException ex) {
                LOGGER.error("Error with recipient <{}>", recipient.toString(), ex);
            }
        }
        executor.shutdown();
        try {
            if (executor.awaitTermination(timeout, TimeUnit.SECONDS)) {
                LOGGER.info("ExecutorService terminated: email sent to all recipients.");
            } else {
                LOGGER.warn("ExecutorService timeout elapsed: some emails may not have been sent.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void processAttachments(EmailModel model) {
        for (EmailModel.Attachment attachment : model.getAttachments()) {
            try {
                EmailAttachment emailAttachment = new EmailAttachment();
                emailAttachment.setDisposition(EmailAttachment.ATTACHMENT);
                emailAttachment.setURL(new URL(attachment.url));
                emailAttachment.setName(attachment.fileName);
                emailAttachment.setDescription(attachment.description);
                email.attach(emailAttachment);
            } catch (MalformedURLException ex) {
                LOGGER.error("Malformed attachment.url '{}'", attachment.url, ex);
            } catch (EmailException ex) {
                LOGGER.error("Error with attachment. {}", attachment.toString(), ex);
            }
        }
    }
}
