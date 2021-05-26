package com.softinstigate.ermes.mail;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Start MailHog mock SMTP server first: https://github.com/mailhog/MailHog
 */
class EmailServiceIT {

        @Test
        void send() throws ExecutionException, InterruptedException {
                SMTPConfig smtpConfig = new SMTPConfig("localhost", 1025, "user", "password", false);

                EmailModel emailModel = new EmailModel("dick.silly@domain.com", "Dick Silly",
                                "Integration Test - " + System.currentTimeMillis(),
                                "This is a <strong>HTML</strong> message.");
                emailModel.addTo("john.doe@email.com", "John Doe");
                emailModel.addTo("serena.wiliamns@email.com", "Serena Wiliams");
                emailModel.addCc("tom.clancy@email.com", "Tom Clancy");
                emailModel.addBcc("ann.smith@email.com", "Ann Smith");

                EmailService emailService = new EmailService(smtpConfig, 3);
                List<String> errors = emailService.sendSynch(emailModel);

                emailService.shutdown();
                assertTrue(errors.isEmpty());
        }

}