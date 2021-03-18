package com.softinstigate.ermes.mail;

import org.apache.commons.mail.EmailException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MailServiceIT {

    @Test
    /**
     * Start MailHog mock SMTP server first: https://github.com/mailhog/MailHog
     */
    void send() throws EmailException {
        SMTPConfig smtpConfig = new SMTPConfig(
                "localhost",
                1025,
                "user",
                "password",
                false);
        MailService mail = new MailService(smtpConfig);
        MailModel model = new MailModel(
                "Testo di <strong>prova</strong>.",
                "sender@domain.com",
                "Dick Silly",
                "Sender");
        model.addRecipient("recipient@email.com", "John Doe", "Test");
        mail.send(model);
    }
}