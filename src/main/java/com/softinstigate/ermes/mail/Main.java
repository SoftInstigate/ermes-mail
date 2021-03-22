package com.softinstigate.ermes.mail;

import org.apache.commons.mail.EmailException;

public class Main {
    public static void main(String[] args) throws EmailException {
        SMTPConfig smtpConfig = new SMTPConfig(
                "localhost",
                1025,
                "user",
                "password",
                false);
        EmailModel emailModel = new EmailModel(
                "sender@domain.com",
                "Nome Cognome",
                "Test",
                "This is a <strong>HTML</strong> message.");
        emailModel.addRecipient("maurizio@email.com", "Maurizio");

        EmailService emailService = new EmailService(smtpConfig);
        emailService.send(emailModel);
    }
}
