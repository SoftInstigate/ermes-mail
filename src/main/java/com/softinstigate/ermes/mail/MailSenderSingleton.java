/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softinstigate.ermes.mail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;

/**
 *
 * @author omar
 */
public class MailSenderSingleton {

    
    private static final HashSet<String> CONFIG_ENV_VARS = new HashSet<>();
    private static final String SMTP_HOSTNAME = "SMTP_HOSTNAME";
    private static final String SMTP_PORT = "SMTP_PORT";
    private static final String FROM_EMAIL = "FROM_EMAIL";
    private static final String SMTP_USERNAME = "SMTP_USERNAME";
    private static final String SMTP_PASSWORD = "SMTP_PASSWORD";
    private static MailSenderSingleton INSTANCE;
    
    
    static {
        CONFIG_ENV_VARS.add(SMTP_PORT);
        CONFIG_ENV_VARS.add(SMTP_HOSTNAME);
        CONFIG_ENV_VARS.add(FROM_EMAIL);
        CONFIG_ENV_VARS.add(SMTP_USERNAME);
        CONFIG_ENV_VARS.add(SMTP_PASSWORD);
    }
    
    private HashMap<String, String> conf;
    
    private MailSenderSingleton() throws MailSenderConfigurationException{
        conf = new HashMap<>();
        
        var missingProps = new HashSet<String>();
        
        CONFIG_ENV_VARS.forEach(env -> {
            var value = System.getenv(env);
            
            if (value == null) {
                missingProps.add(env);
                return;
            }
            
            conf.put(env, value);
        });
        
        if (!missingProps.isEmpty()) {
            throw new MailSenderConfigurationException(missingProps);
        }
    }
    
    public String sendTextEmail(String address, String subject, String message ) {
        
        System.out.format("from='%s',  to='%s', subject='%s', message='%s'\n", conf.get("FROM_EMAIL"), address, subject, message);

        var email = new SimpleEmail();

        email.setHostName(conf.get(SMTP_HOSTNAME));
        email.setSmtpPort(Integer.parseInt(conf.get(SMTP_PORT)));
        email.setAuthenticator(new DefaultAuthenticator(
                conf.get(SMTP_USERNAME), 
                conf.get(SMTP_PASSWORD)
        ));
        email.setSSLOnConnect(true);
        
        String result = null;
        
        try {
            email.setSubject(subject);
            email.setFrom(conf.get(FROM_EMAIL));
            email.addTo(address);
            email.setMsg(message);
            result = email.send();
        } catch (EmailException ex) {
            Logger.getLogger(MailSenderSingleton.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;
    }
    
    public String sendHtmlEmail(String address, String subject, String html ) {
        
        System.out.format("from='%s',  to='%s', subject='%s', message='%s'\n", conf.get("FROM_EMAIL"), address, subject, html);

        var email = new HtmlEmail();

        email.setHostName(conf.get(SMTP_HOSTNAME));
        email.setSmtpPort(Integer.parseInt(conf.get(SMTP_PORT)));
        email.setAuthenticator(new DefaultAuthenticator(
                conf.get(SMTP_USERNAME), 
                conf.get(SMTP_PASSWORD)
        ));
        email.setSSLOnConnect(true);
        
        String result = null;
        
        try {
            email.setSubject(subject);
            email.setFrom(conf.get(FROM_EMAIL));
            email.addTo(address);
            email.setHtmlMsg(html);
            result = email.send();
        } catch (EmailException ex) {
            Logger.getLogger(MailSenderSingleton.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;
    }

    public static MailSenderSingleton getInstance() throws MailSenderConfigurationException {
        if (INSTANCE == null) {
            INSTANCE = new MailSenderSingleton();
        }

        return INSTANCE;

    }

}
