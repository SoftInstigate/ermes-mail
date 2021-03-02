/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softinstigate.ermes.mail;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author omar
 */
public class MailSender {

    private final String SMTP_HOSTNAME = "SMTP_HOSTNAME";
    private final String SMTP_PORT = "SMTP_PORT";
    private final String FROM_EMAIL = "FROM_EMAIL";
    private final String SMTP_USERNAME = "SMTP_USERNAME";
    private final String SMTP_PASSWORD = "SMTP_PASSWORD";

    private final HashMap<String, String> conf;

    public MailSender() throws MailConfigurationException {
        conf = new HashMap<>();
        Set<String> CONFIG_ENV_VARS = new HashSet<>();
        Collections.addAll(CONFIG_ENV_VARS, SMTP_PORT, SMTP_HOSTNAME, FROM_EMAIL, SMTP_USERNAME, SMTP_PASSWORD);

        Set<String> missingProps = new HashSet<>();

        CONFIG_ENV_VARS.forEach(env -> {
            var value = System.getenv(env);

            if (value == null) {
                missingProps.add(env);
                return;
            }

            conf.put(env, value);
        });

        if (!missingProps.isEmpty()) {
            throw new MailConfigurationException(missingProps);
        }
    }

    public String sendTextEmail(String address, String subject, String textMessage) throws EmailException {
        System.out.format("from='%s', to='%s', subject='%s', message='%s'\n", conf.get("FROM_EMAIL"), address, subject,
                textMessage);
        SimpleEmail email = new SimpleEmail();
        setEmailParameters(address, subject, email);
        email.setMsg(textMessage);
        return email.send();
    }

    public String sendHtmlEmail(String address, String subject, String htmlMessage) throws EmailException {
        System.out.format("from='%s',  to='%s', subject='%s', message='%s'\n", conf.get("FROM_EMAIL"), address, subject,
                htmlMessage);
        HtmlEmail email = new HtmlEmail();
        setEmailParameters(address, subject, email);
        email.setHtmlMsg(htmlMessage);
        return email.send();
    }

    private void setEmailParameters(String address, String subject, Email email) throws EmailException {
        email.setHostName(conf.get(SMTP_HOSTNAME));
        email.setSmtpPort(Integer.parseInt(conf.get(SMTP_PORT)));
        email.setAuthenticator(new DefaultAuthenticator(conf.get(SMTP_USERNAME), conf.get(SMTP_PASSWORD)));
        email.setSSLOnConnect(true);
        email.setSubject(subject);
        email.setFrom(conf.get(FROM_EMAIL));
        email.addTo(address);
    }

}
