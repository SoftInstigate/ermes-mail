package com.softinstigate.ermes.mail;

import org.apache.commons.mail.EmailException;

public class Main {
    public static void main(String[] args) throws EmailException, InterruptedException {
        SMTPConfig smtpConfig = new SMTPConfig(
                "localhost",
                1025,
                "user",
                "password",
                false);
        MailModel mailModel = new MailModel(
                "sender@domain.com",
                "Nome Cognome",
                "Prova",
                "Testo di <strong>prova</strong>.");
        mailModel.addRecipient("maurizio@email.com", "Maurizio");

        MailService mailService = new MailService(smtpConfig, 1);
        mailService.send(mailModel);
        mailService.shutdown(5);
    }
}
