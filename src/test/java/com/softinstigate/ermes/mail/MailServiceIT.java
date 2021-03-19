package com.softinstigate.ermes.mail;

import org.apache.commons.mail.EmailException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

class MailServiceIT {

    @Test
    /**
     * Start MailHog mock SMTP server first: https://github.com/mailhog/MailHog
     */
    void send() throws EmailException, InterruptedException {
        SMTPConfig smtpConfig = new SMTPConfig(
                "localhost",
                1025,
                "user",
                "password",
                false);

        MailModel mailModel = new MailModel(
                "dick.silly@domain.com",
                "Dick Silly",
                "Integration Test - " + System.currentTimeMillis(),
                "Testo di <strong>prova</strong>."
        );
        mailModel.addRecipient("john.doe@email.com", "John Doe");

        CountDownLatch countdownLatch = new CountDownLatch(1);

        MailService mailService = new MailService(smtpConfig, 1);
        mailService.send(mailModel);
        mailService.shutdown(5);

        countdownLatch.countDown();
        countdownLatch.await();
    }
}