package com.softinstigate.ermes.mail;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Runnable class to invoke the email.send() method in a thread
 */
public class SendEmailTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendEmailTask.class);

    private final HtmlEmail email;
    private final EmailModel model;

    public SendEmailTask(HtmlEmail email, EmailModel model) {
        this.email = email;
        this.model = model;
    }

    @Override
    public void run() {
        LOGGER.info("Processing {}", model.toString());
        try {
            email.setFrom(model.from, model.senderFullName);
            email.setSubject(model.subject);
            email.setMsg(model.message);
            for (EmailModel.Recipient recipient : model.getRecipients()) {
                try {
                    email.addTo(recipient.email, recipient.name);
                    processAttachments(model);
                    email.send();
                } catch (EmailException ex) {
                    LOGGER.error("Error with recipient <{}>", recipient.toString(), ex);
                }
            }
            LOGGER.info("Email successfully sent to recipients: {}", email.getToAddresses());
        } catch (EmailException e) {
            LOGGER.error("Error sending emails: {}", email.toString(), e);
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
