package com.softinstigate.ermes.mail;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
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

        MailTask mailTask = new MailTask(email, model);
        mailTask.run();
    }
}
