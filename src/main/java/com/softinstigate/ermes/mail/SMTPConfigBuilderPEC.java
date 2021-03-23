package com.softinstigate.ermes.mail;

public class SMTPConfigBuilderPEC extends SMTPConfigAbstractBuilder {

    public SMTPConfigBuilderPEC() {
        super("PEC_SMTP_HOSTNAME",
                "PEC_SMTP_PORT",
                "PEC_SMTP_USERNAME",
                "PEC_SMTP_PASSWORD",
                "PEC_SMTP_SSL_ON",
                "PEC_SMTP_SSL_PORT"
        );
    }
}
