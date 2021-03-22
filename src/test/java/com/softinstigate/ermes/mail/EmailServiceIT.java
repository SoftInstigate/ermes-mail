package com.softinstigate.ermes.mail;

import org.apache.commons.mail.EmailException;
import org.junit.jupiter.api.Test;

class EmailServiceIT {
    /**
     * Start MailHog mock SMTP server first: https://github.com/mailhog/MailHog
     */
    @Test
    void send() throws EmailException {
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
        emailService.send(emailModel);
        emailService.shutdown();
    }
}