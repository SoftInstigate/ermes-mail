package com.softinstigate.ermes.mail;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class EmailServiceIT {
    /**
     * Start MailHog mock SMTP server first: https://github.com/mailhog/MailHog
     */
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
        Future<List<String>> errors = emailService.send(emailModel);

        assertTrue(errors.get().isEmpty());

        emailService.shutdown();
    }
}