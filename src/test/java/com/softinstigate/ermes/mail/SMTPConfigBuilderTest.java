package com.softinstigate.ermes.mail;

import org.junit.jupiter.api.Test;

class SMTPConfigBuilderTest {

    @Test
    void build() {
        System.setProperty("SMTP_HOSTNAME", "test.host");
        System.setProperty("SMTP_PORT", "25");
        System.setProperty("SMTP_USERNAME", "test-user");
        System.setProperty("SMTP_PASSWORD", "test-password");
        System.setProperty("SMTP_SSL_ON", "true");
        System.setProperty("SMTP_SSL_PORT", "465");

        SMTPConfigBuilder configBuilder = new SMTPConfigBuilder();
        SMTPConfig config = configBuilder.build();
        System.out.println(config);
    }
}