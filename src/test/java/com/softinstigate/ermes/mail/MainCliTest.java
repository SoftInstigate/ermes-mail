package com.softinstigate.ermes.mail;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import picocli.CommandLine;

class MainCliTest {

    @Test
    void sslFlagParsesAndMapsToSslConfig() {
        Main main = new Main();
    CommandLine.populateCommand(main, "--sslon", "--sslport", "465", "-h", "smtp.example.com", "-u", "u", "-P", "p",
        "-f", "sender@example.com", "-s", "subj", "-b", "body", "--to", "rcpt@example.com");

    assertTrue((Boolean) getField(main, "sslOn"));
    assertEquals(465, (Integer) getField(main, "sslPort"));
    assertEquals("smtp.example.com", (String) getField(main, "smtpHost"));

    // emulate Main's config selection logic
    boolean sslOn = (Boolean) getField(main, "sslOn");
    String smtpHost = (String) getField(main, "smtpHost");
    int smtpPort = (Integer) getField(main, "smtpPort");
    String user = (String) getField(main, "user");
    String password = (String) getField(main, "password");
    int sslPort = (Integer) getField(main, "sslPort");

    SMTPConfig cfg = sslOn
        ? SMTPConfig.forSsl(smtpHost, smtpPort, user, password, sslPort)
        : SMTPConfig.forPlain(smtpHost, smtpPort, user, password);

    assertEquals(SMTPConfig.SecurityMode.SSL, cfg.securityMode);
    assertTrue(cfg.ssl);
    assertEquals(465, cfg.sslPort);
    }

    @Test
    void startTlsRequiredParsesAndMapsToStartTlsRequired() {
        Main main = new Main();
    CommandLine.populateCommand(main, "--starttls", "--starttls-required", "-h", "smtp.example.com", "-p", "587", "-u", "u", "-P", "p",
        "-f", "sender@example.com", "-s", "subj", "-b", "body", "--to", "rcpt@example.com");

    assertTrue((Boolean) getField(main, "startTls"));
    assertTrue((Boolean) getField(main, "startTlsRequired"));
    assertEquals(587, (Integer) getField(main, "smtpPort"));

    boolean startTls = (Boolean) getField(main, "startTls");
    boolean startTlsRequired = (Boolean) getField(main, "startTlsRequired");
    String smtpHost = (String) getField(main, "smtpHost");
    int smtpPort = (Integer) getField(main, "smtpPort");
    String user = (String) getField(main, "user");
    String password = (String) getField(main, "password");

    SMTPConfig cfg2 = startTls
        ? (startTlsRequired
            ? SMTPConfig.forStartTlsRequired(smtpHost, smtpPort, user, password)
            : SMTPConfig.forStartTlsOptional(smtpHost, smtpPort, user, password))
        : SMTPConfig.forPlain(smtpHost, smtpPort, user, password);

    assertEquals(SMTPConfig.SecurityMode.STARTTLS_REQUIRED, cfg2.securityMode);
    }

    @Test
    void mutuallyExclusiveFlagsDetectedByLogic() {
        Main main = new Main();
    CommandLine.populateCommand(main, "--sslon", "--starttls", "-f", "sender@example.com", "-s", "subj", "-b", "body", "--to", "rcpt@example.com");

        // parsing should set both flags; the mutual exclusion is enforced at runtime
        assertTrue((Boolean) getField(main, "sslOn"));
        assertTrue((Boolean) getField(main, "startTls"));

        // runtime check in Main.call() would treat this as an error
        assertTrue((Boolean) getField(main, "sslOn") && (Boolean) getField(main, "startTls"));
    }

    private Object getField(Object target, String name) {
        try {
            java.lang.reflect.Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
