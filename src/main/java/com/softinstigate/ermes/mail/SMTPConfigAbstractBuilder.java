package com.softinstigate.ermes.mail;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class SMTPConfigAbstractBuilder {

    protected final String SMTP_HOSTNAME;
    protected final String SMTP_PORT;
    protected final String SMTP_USERNAME;
    protected final String SMTP_PASSWORD;
    protected final String SMTP_SSL_ON;     // optional, default is 'false'
    protected final String SMTP_SSL_PORT;   // optional, default is "465"

    protected SMTPConfigAbstractBuilder(String smtp_hostname, String smtp_port, String smtp_username, String smtp_password, String smtp_ssl_on, String smtp_ssl_port) {
        SMTP_HOSTNAME = smtp_hostname;
        SMTP_PORT = smtp_port;
        SMTP_USERNAME = smtp_username;
        SMTP_PASSWORD = smtp_password;
        SMTP_SSL_ON = smtp_ssl_on;
        SMTP_SSL_PORT = smtp_ssl_port;
    }

    public SMTPConfig build() {
        Set<String> CONFIG_ENV_VARS = new HashSet<>();
        Collections.addAll(CONFIG_ENV_VARS, SMTP_HOSTNAME, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD);
        Set<String> missingProps = new HashSet<>();
        HashMap<String, String> conf = new HashMap<>();
        CONFIG_ENV_VARS.forEach(prop -> {
            var value = System.getProperty(prop);
            if (value == null) {
                missingProps.add(prop);
                return;
            }
            conf.put(prop, value);
        });

        if (!missingProps.isEmpty()) {
            throw new EmailConfigurationException(missingProps);
        }

        return new SMTPConfig(
                conf.get(SMTP_HOSTNAME),
                Integer.parseInt(conf.get(SMTP_PORT)),
                conf.get(SMTP_USERNAME),
                conf.get(SMTP_PASSWORD),
                Boolean.parseBoolean(conf.getOrDefault(SMTP_SSL_ON, "false")),
                Integer.parseInt(conf.getOrDefault(SMTP_SSL_PORT, String.valueOf(SMTPConfig.DEFAULT_SSL_PORT)))
        );
    }
}
