package com.softinstigate.ermes.mail;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.mail.EmailException;

public class Main {

    private static final HashSet<String> ENV_SET = new HashSet<>();

    static {
        ENV_SET.add("SMTP_PORT");
        ENV_SET.add("SMTP_HOSTNAME");
        ENV_SET.add("FROM_EMAIL");
        ENV_SET.add("USERNAME");
        ENV_SET.add("PASSWORD");
    }

    public static void main(String[] args) throws EmailException, IOException {
        String emailType = args[0];

        MailSender message = new MailSender();

        if (emailType.equals("text")) {
            message.sendTextEmail(args[1], args[2], args[3]);
        } else if (emailType.equals("html") || emailType.equals("html-template")) {

            String htmlMsg = null;

            if (emailType.equals("html-template")) {
                var vars = new HashMap<String, String>();
                String _vars = args[4];
                String[] pairs = _vars.split(",");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":");
                    vars.put(keyValue[0], keyValue[1]);
                }
                htmlMsg = HtmlUtils.parseTemplate(args[3], vars);

            } else {
                htmlMsg = args[3];
            }
            message.sendHtmlEmail(args[1], args[2], htmlMsg);

        } else {
            System.out.println("Unrecognized email type");
        }
    }
}
