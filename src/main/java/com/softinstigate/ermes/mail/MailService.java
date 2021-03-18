package com.softinstigate.ermes.mail;

import org.apache.commons.mail.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    private final SMTPConfig config;

    public MailService(SMTPConfig config) {
        this.config = config;
        LOGGER.debug(config.toString());
    }

    public void send(MailModel model) throws EmailException {
        LOGGER.debug(model.toString());
        HtmlEmail email = new HtmlEmail();
        email.setHostName(config.hostname);
        email.setSmtpPort(config.port);
        email.setAuthentication(config.username, config.password);
        email.setSSLOnConnect(config.ssl);
        email.setSslSmtpPort(config.sslPort);

        for (MailModel.Recipient recipient : model.getRecipients()) {
            email.setFrom(model.from, model.sender);
            email.setSubject(recipient.subject);
            email.addTo(recipient.email, recipient.name);
            email.setMsg(model.message);

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
        }
    }
}
