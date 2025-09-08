package com.softinstigate.ermes.mail;

import static org.junit.jupiter.api.Assertions.*;

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
        assertTrue(s.contains("securityMode=SSL") || s.contains("securityMode=SSL"));
    }
}
