package com.softinstigate.ermes.mail;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class Sender {

    public static void main(String[] args) throws EmailException {
        String from = args[0];
        String to = args[1];
        String object = args[2];
        String message = args[3];
        String hostName = args[4];
        int smtpPort = Integer.parseInt(args[5]);
        String username = args[6];
        String password = args[7];

        System.out.format("from='%s',  to='%s', message='%s'\n", from, to, message);

        var email = new SimpleEmail();

        email.setHostName(hostName);
        email.setSmtpPort(smtpPort);
        email.setAuthenticator(new DefaultAuthenticator(username, password));
        email.setSSLOnConnect(true);
        email.setFrom(from);
        email.setSubject(object);
        email.setMsg(message);
        email.addTo(to);
        email.send();
    }
}
