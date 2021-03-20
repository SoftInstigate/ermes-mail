package com.softinstigate.ermes.mail;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runnable class to invoke the email.send() method in a thread
 */
public class SendEmailTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendEmailTask.class);

    private final Email email;

    public SendEmailTask(Email email) {
        this.email = email;
    }

    @Override
    public void run() {
        try {
            email.send();
            LOGGER.debug("Email successfully sent to recipients: {}", email.getToAddresses());
        } catch (EmailException e) {
            LOGGER.error("Error sending email: {}", email.toString(), e);
        }
    }

}
