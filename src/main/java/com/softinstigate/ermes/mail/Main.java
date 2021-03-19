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
        MailService mail = new MailService(smtpConfig);
        MailModel model = new MailModel(
                "sender@domain.com",
                "Nome Cognome",
                "Prova",
                "Testo di <strong>prova</strong>.");
        model.addRecipient("maurizio@email.com", "Maurizio");
        mail.send(model);
    }
}
