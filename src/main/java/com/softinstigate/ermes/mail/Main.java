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
                "Testo di <strong>prova</strong>.",
                "sender@domain.com",
                "Nome Cognome",
                "Sender");
        model.addRecipient("maurizio@email.com", "Maurizio", "Prova");
        mail.send(model);
    }
}
