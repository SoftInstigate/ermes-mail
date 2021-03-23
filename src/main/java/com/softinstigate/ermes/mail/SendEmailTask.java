package com.softinstigate.ermes.mail;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Runnable class to invoke the email.send() method in a thread
 */
public class SendEmailTask implements Callable<List<String>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendEmailTask.class);

    private final SMTPConfig smtpConfig;
    private final EmailModel model;

    public SendEmailTask(SMTPConfig smtpConfig, EmailModel model) {
        this.smtpConfig = smtpConfig;
        this.model = model;
    }

    /**
     * Send the EmailModel using a HtmlEmail instance
     *
     * @return a Future<List<String>> of errors. If the list is empty then no errors!
     */
    @Override
    public List<String> call() {
        LOGGER.info("Processing {}", model.toString());

        final List<String> errors = new ArrayList<>();
        HtmlEmail email = new HtmlEmail();
        email.setHostName(smtpConfig.hostname);
        email.setSmtpPort(smtpConfig.port);
        email.setAuthentication(smtpConfig.username, smtpConfig.password);
        email.setSSLOnConnect(smtpConfig.ssl);
        email.setSslSmtpPort(String.valueOf(smtpConfig.sslPort));
        try {
            email.setFrom(model.from, model.senderFullName);
            email.setSubject(model.subject);
            email.setMsg(model.message);
            for (EmailModel.Recipient recipient : model.getRecipients()) {
                try {
                    email.addTo(recipient.email, recipient.name);
                    processAttachments(email, model, errors);
                    email.send();
                    LOGGER.info("Email successfully sent to recipient <{}>", recipient.email);
                } catch (EmailException ex) {
                    LOGGER.error("Error sending email to <{}>", recipient.email, ex);
                    errors.add(String.format("Error sending email to <%s>: '%s'", recipient.email, ex.getMessage()));
                }
            }
        } catch (EmailException ex) {
            LOGGER.error("Email client error", ex);
            errors.add(String.format("Email client error '%s'", ex.getMessage()));
        }
        return errors;
    }

    /**
     * Attach included attachments to email
     *
     * @param email a HtmlEmail instance
     * @param model the EmailModel to process
     */
    private void processAttachments(HtmlEmail email, EmailModel model, List<String> errors) {
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
                errors.add(String.format("Malformed attachment.url '%s'", ex.getMessage()));
            } catch (EmailException ex) {
                LOGGER.error("Error with attachment '{}'", attachment.toString(), ex);
                errors.add(String.format("Error with attachment '%s'", ex.getMessage()));
            }
        }
    }
}
