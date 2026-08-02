package com.softinstigate.ermes.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SMTPConfigTest {

    @Test
    void factoriesProduceExpectedSecurityModesAndFields() {
        SMTPConfig plain = SMTPConfig.forPlain("localhost", 25, "", "");
        assertEquals(SMTPConfig.SecurityMode.PLAIN, plain.securityMode);
        assertFalse(plain.ssl);

        SMTPConfig ssl = SMTPConfig.forSsl("smtp.example.com", 25, "u", "p", 465);
        assertEquals(SMTPConfig.SecurityMode.SSL, ssl.securityMode);
        assertTrue(ssl.ssl);
        assertEquals(465, ssl.sslPort);

        SMTPConfig startOpt = SMTPConfig.forStartTlsOptional("smtp.example.com", 587, "u", "p");
        assertEquals(SMTPConfig.SecurityMode.STARTTLS_OPTIONAL, startOpt.securityMode);

        SMTPConfig startReq = SMTPConfig.forStartTlsRequired("smtp.example.com", 587, "u", "p");
        assertEquals(SMTPConfig.SecurityMode.STARTTLS_REQUIRED, startReq.securityMode);

        String s = ssl.toString();
        assertTrue(s.contains("smtp.example.com"));
        assertTrue(s.contains("securityMode=SSL"));
    }

    @Test
    void forPlainAssignsAllFields() {
        SMTPConfig cfg = SMTPConfig.forPlain("smtp.host", 25, "user", "pass");

        assertEquals("smtp.host", cfg.hostname);
        assertEquals(25, cfg.port);
        assertEquals("user", cfg.username);
        assertEquals("pass", cfg.password);
        assertFalse(cfg.ssl);
        assertEquals(SMTPConfig.DEFAULT_SSL_PORT, cfg.sslPort);
        assertEquals(SMTPConfig.SecurityMode.PLAIN, cfg.securityMode);
    }

    @Test
    void forSslAssignsAllFields() {
        SMTPConfig cfg = SMTPConfig.forSsl("smtp.host", 465, "user", "pass", 465);

        assertEquals("smtp.host", cfg.hostname);
        assertEquals(465, cfg.port);
        assertEquals("user", cfg.username);
        assertEquals("pass", cfg.password);
        assertTrue(cfg.ssl);
        assertEquals(465, cfg.sslPort);
        assertEquals(SMTPConfig.SecurityMode.SSL, cfg.securityMode);
    }

    @Test
    void forSslWithCustomSslPort() {
        SMTPConfig cfg = SMTPConfig.forSsl("smtp.host", 25, "u", "p", 587);
        assertEquals(587, cfg.sslPort);
    }

    @Test
    void forStartTlsOptionalAssignsAllFields() {
        SMTPConfig cfg = SMTPConfig.forStartTlsOptional("smtp.host", 587, "user", "pass");

        assertEquals("smtp.host", cfg.hostname);
        assertEquals(587, cfg.port);
        assertEquals("user", cfg.username);
        assertEquals("pass", cfg.password);
        assertFalse(cfg.ssl);
        assertEquals(SMTPConfig.DEFAULT_SSL_PORT, cfg.sslPort);
        assertEquals(SMTPConfig.SecurityMode.STARTTLS_OPTIONAL, cfg.securityMode);
    }

    @Test
    void forStartTlsRequiredAssignsAllFields() {
        SMTPConfig cfg = SMTPConfig.forStartTlsRequired("smtp.host", 587, "user", "pass");

        assertEquals("smtp.host", cfg.hostname);
        assertEquals(587, cfg.port);
        assertEquals("user", cfg.username);
        assertEquals("pass", cfg.password);
        assertFalse(cfg.ssl);
        assertEquals(SMTPConfig.DEFAULT_SSL_PORT, cfg.sslPort);
        assertEquals(SMTPConfig.SecurityMode.STARTTLS_REQUIRED, cfg.securityMode);
    }

    @Test
    void defaultSslPortIs465() {
        assertEquals(465, SMTPConfig.DEFAULT_SSL_PORT);
    }

    @Test
    void toStringRedactsUsername() {
        SMTPConfig cfg = SMTPConfig.forPlain("smtp.host", 25, "myuser", "mypass");

        String s = cfg.toString();
        assertTrue(s.contains("[REDACTED]"), "Expected [REDACTED] in toString, got: " + s);
        assertFalse(s.contains("myuser"), "toString should not contain actual username");
    }

    @Test
    void toStringShowsNullForEmptyUsername() {
        SMTPConfig cfg = SMTPConfig.forPlain("smtp.host", 25, "", "");

        String s = cfg.toString();
        assertTrue(s.contains("null"), "Expected 'null' for empty username in toString, got: " + s);
    }

    @Test
    void toSecureStringShowsHasCredentialsTrue() {
        SMTPConfig cfg = SMTPConfig.forPlain("smtp.host", 25, "user", "pass");

        String s = cfg.toSecureString();
        assertTrue(s.contains("hasCredentials=true"), "Expected hasCredentials=true, got: " + s);
        assertFalse(s.contains("pass"), "toSecureString should not contain password");
    }

    @Test
    void toSecureStringShowsHasCredentialsFalse() {
        SMTPConfig cfg = SMTPConfig.forPlain("smtp.host", 25, "", "");

        String s = cfg.toSecureString();
        assertTrue(s.contains("hasCredentials=false"), "Expected hasCredentials=false, got: " + s);
    }
}
