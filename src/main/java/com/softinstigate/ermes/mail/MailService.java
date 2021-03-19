package com.softinstigate.ermes.mail;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    private final ExecutorService executor;
    private final HtmlEmail email = new HtmlEmail();

    public MailService(SMTPConfig config, int threadPoolSize) {
        executor = Executors.newFixedThreadPool(threadPoolSize);

        email.setHostName(config.hostname);
        email.setSmtpPort(config.port);
        email.setAuthentication(config.username, config.password);
        email.setSSLOnConnect(config.ssl);
        email.setSslSmtpPort(config.sslPort);

        LOGGER.debug(config.toString());
        LOGGER.info("MailService initialized.");
    }

    public void send(MailModel model) throws EmailException {
        LOGGER.debug(model.toString());
        MailTask mailTask = new MailTask(this.email, model);
        executor.execute(mailTask);
    }

    public void shutdown(long timeout) throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(timeout, TimeUnit.SECONDS);
        LOGGER.info("MailService is shut down.");
    }
}
