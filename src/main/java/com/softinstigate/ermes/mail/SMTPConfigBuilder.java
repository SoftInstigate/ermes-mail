package com.softinstigate.ermes.mail;

public class SMTPConfigBuilder extends SMTPConfigAbstractBuilder {

    public SMTPConfigBuilder() {
        super("SMTP_HOSTNAME",
                "SMTP_PORT",
                "SMTP_USERNAME",
                "SMTP_PASSWORD",
                "SMTP_SSL_ON",
                "SMTP_SSL_PORT"
        );
    }
}
