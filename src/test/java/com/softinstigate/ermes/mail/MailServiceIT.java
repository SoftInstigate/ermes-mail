package com.softinstigate.ermes.mail;

import org.apache.commons.mail.EmailException;
import org.junit.jupiter.api.Test;

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
                "dick.silly@domain.com",
                "Dick Silly",
                "Integration Test",
                "Testo di <strong>prova</strong>."
        );
        model.addRecipient("john.doe@email.com", "John Doe");
        mail.send(model);
    }
}