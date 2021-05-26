package com.softinstigate.ermes.mail;

import org.junit.jupiter.api.Test;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Start MailHog mock SMTP server first: https://github.com/mailhog/MailHog
 */
class EmailServiceIT {

    private static final Logger LOGGER = Logger.getLogger(EmailServiceIT.class.getName());

    @Test
    void send() throws ExecutionException, InterruptedException {
        SMTPConfig smtpConfig = new SMTPConfig(
                "localhost",
                1025,
                "user",
                "password",
                false);

        EmailModel emailModel = new EmailModel(
                "dick.silly@domain.com",
                "Dick Silly",
                "Integration Test - " + System.currentTimeMillis(),
                "This is a <strong>HTML</strong> message."
        );
        emailModel.addRecipient("john.doe@email.com", "John Doe");

        EmailService emailService = new EmailService(smtpConfig, 3);
        List<String> errors = emailService.sendSynch(emailModel);

        emailService.shutdown();
        assertTrue(errors.isEmpty());
    }

    @Test
    void sendFailure() throws ExecutionException, InterruptedException {
        SMTPConfig smtpConfig = new SMTPConfig(
                "localhost",
                25,
                "user",
                "password",
                false);

        EmailModel emailModel = new EmailModel(
                "dick.silly@domain.com",
                "Dick Silly",
                "Integration Test - " + System.currentTimeMillis(),
                "This is a <strong>HTML</strong> message."
        );
        emailModel.addRecipient("john.doe@email.com", "John Doe");

        EmailService emailService = new EmailService(smtpConfig, 3);
        Future<List<String>> futureErrors = emailService.send(emailModel);

        List<String> errors = futureErrors.get();
        emailService.shutdown();

        LOGGER.severe("Errors: " + errors.toString());
        assertFalse(errors.isEmpty());
    }
}