package com.softinstigate.ermes.mail;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SMTPConfigFromEnvironment {

    private final String SMTP_HOSTNAME = "SMTP_HOSTNAME";
    private final String SMTP_PORT = "SMTP_PORT";
    private final String FROM_EMAIL = "FROM_EMAIL";
    private final String SMTP_USERNAME = "SMTP_USERNAME";
    private final String SMTP_PASSWORD = "SMTP_PASSWORD";
    private final String SMTP_SSL_ON = "SMTP_SSL_ON";       // optional, default is 'false'
    private final String SMTP_SSL_PORT = "SMTP_SSL_PORT";   // optional, default is "465"

    public SMTPConfigFromEnvironment() {
    }

    public SMTPConfig build() {
        Set<String> CONFIG_ENV_VARS = new HashSet<>();
        Collections.addAll(CONFIG_ENV_VARS, SMTP_PORT, SMTP_HOSTNAME, FROM_EMAIL, SMTP_USERNAME, SMTP_PASSWORD);
        Set<String> missingProps = new HashSet<>();
        HashMap<String, String> conf = new HashMap<>();
        CONFIG_ENV_VARS.forEach(env -> {
            var value = System.getenv(env);
            if (value == null) {
                missingProps.add(env);
                return;
            }
            conf.put(env, value);
        });

        if (!missingProps.isEmpty()) {
            throw new EmailConfigurationException(missingProps);
        }

        return new SMTPConfig(
                conf.get(SMTP_HOSTNAME),
                Integer.parseInt(conf.get(SMTP_PORT)),
                conf.get(SMTP_HOSTNAME),
                conf.get(SMTP_PASSWORD),
                Boolean.parseBoolean(conf.getOrDefault(SMTP_SSL_ON, "false")),
                conf.getOrDefault(SMTP_SSL_PORT, SMTPConfig.DEFAULT_SSL_PORT)
        );
    }
}