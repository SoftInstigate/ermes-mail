package com.softinstigate.ermes.mail;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class MailTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailTask.class);

    private final MailModel model;
    private final HtmlEmail email;

    public MailTask(HtmlEmail email, MailModel model) throws EmailException {
        this.email = email;
        this.model = model;
        this.email.setFrom(model.from, model.senderFullName);
        this.email.setSubject(model.subject);
        this.email.setMsg(model.message);
    }

    @Override
    public void run() {
        for (MailModel.Recipient recipient : model.getRecipients()) {
            try {
                email.addTo(recipient.email, recipient.name);
                try {
                    for (MailModel.Attachment attachment : model.getAttachments()) {
                        EmailAttachment emailAttachment = new EmailAttachment();
                        emailAttachment.setDisposition(EmailAttachment.ATTACHMENT);
                        emailAttachment.setURL(new URL(attachment.url));
                        emailAttachment.setName(attachment.fileName);
                        emailAttachment.setDescription(attachment.description);
                        email.attach(emailAttachment);
                    }
                } catch (MalformedURLException ex) {
                    LOGGER.error("Error with malformed attachment.url", ex);
                }
                email.send();
                LOGGER.debug("Email sent to " + recipient.email);
            } catch (EmailException e) {
                LOGGER.error("Error sending email to recipient: " + recipient.toString(), e);
            }
        }
    }
}
