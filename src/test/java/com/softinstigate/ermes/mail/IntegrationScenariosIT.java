/*-
 * ========================LICENSE_START=================================
 * ermes-mail
 * %%
 * Copyright (C) 2021 - 2025 SoftInstigate srl
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package com.softinstigate.ermes.mail;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Generic integration tests covering multiple SMTP scenarios.
 *
 * Scenarios:
 *  - local: plain SMTP against Mailpit (localhost:1025)
 *  - external-smtps: implicit SSL/SMTPS against an external provider (requires env or properties)
 */
class IntegrationScenariosIT {

    @TestFactory
    List<DynamicTest> scenarios() {
        List<DynamicTest> tests = new ArrayList<>();

        // Local Mailpit/plain SMTP scenario — only run if Mailpit is reachable on localhost:1025
        tests.add(DynamicTest.dynamicTest("local-plain-mailpit", () -> {
            String mpHost = "localhost";
            int mpPort = 1025;
            boolean mailpitUp = true;
            try (java.net.Socket s = new java.net.Socket()) {
                s.connect(new java.net.InetSocketAddress(mpHost, mpPort), 500);
            } catch (Exception ex) {
                mailpitUp = false;
            }
            Assumptions.assumeTrue(mailpitUp, "Mailpit not reachable on localhost:1025 - skipping local Mailpit test");

            SMTPConfig smtpConfig = SMTPConfig.forPlain(mpHost, mpPort, "user", "password");

            EmailModel emailModel = new EmailModel("dick.silly@domain.com", "Dick Silly",
                    "Integration Test - local plain " + System.currentTimeMillis(),
                    "This is a <strong>HTML</strong> message.");
            emailModel.addTo("john.doe@email.com", "John Doe");

            EmailService emailService = new EmailService(smtpConfig, 3);
            List<String> errors = emailService.sendSynch(emailModel);
            emailService.shutdown();

            assertTrue(errors.isEmpty(), "Errors sending email in local plain scenario: " + errors);
        }));

        // External SMTPS scenario (only executed if configuration is present via env or properties)
        tests.add(DynamicTest.dynamicTest("external-smtps-conditional", () -> {
            // Try env first
            String user = System.getenv("SMTP_INTEGRATION_USERNAME");
            String pass = System.getenv("SMTP_INTEGRATION_PASSWORD");
            String sender = System.getenv("SMTP_INTEGRATION_SENDER");
            String recipient = System.getenv("SMTP_INTEGRATION_RECIPIENT");
            String host = System.getenv("SMTP_INTEGRATION_HOST");
            String portStr = System.getenv("SMTP_INTEGRATION_PORT");

            if (user == null || pass == null || sender == null || recipient == null || host == null || portStr == null) {
                java.util.Properties p = loadFromFiles();
                if (user == null) user = p.getProperty("SMTP_INTEGRATION_USERNAME");
                if (pass == null) pass = p.getProperty("SMTP_INTEGRATION_PASSWORD");
                if (sender == null) sender = p.getProperty("SMTP_INTEGRATION_SENDER");
                if (recipient == null) recipient = p.getProperty("SMTP_INTEGRATION_RECIPIENT");
                if (host == null) host = p.getProperty("SMTP_INTEGRATION_HOST");
                if (portStr == null) portStr = p.getProperty("SMTP_INTEGRATION_PORT");
            }

            boolean present = user != null && pass != null && sender != null && recipient != null && host != null && portStr != null;
            Assumptions.assumeTrue(present, "External SMTPS integration test skipped - missing SMTP_INTEGRATION_* configuration (env or properties file)");

            int port = Integer.parseInt(portStr);
            String sslPortStr = System.getenv("SMTP_INTEGRATION_SSLPORT");
            int sslPort = sslPortStr != null ? Integer.parseInt(sslPortStr) : port;

            // Determine whether to use STARTTLS or implicit SSL (SMTPS).
            // Priority: environment var SMTP_INTEGRATION_STARTTLS, then properties file, then port heuristic (587 -> STARTTLS).
            java.util.Properties cfg = loadFromFiles();
            String starttlsEnv = System.getenv("SMTP_INTEGRATION_STARTTLS");
            if (starttlsEnv == null) starttlsEnv = cfg.getProperty("SMTP_INTEGRATION_STARTTLS");
            boolean useStartTls = false;
            if (starttlsEnv != null) {
                useStartTls = "true".equalsIgnoreCase(starttlsEnv) || "yes".equalsIgnoreCase(starttlsEnv);
            } else {
                // fallback heuristic: use STARTTLS when running against submission port 587
                useStartTls = (port == 587);
            }

            SMTPConfig smtpConfig;
            if (useStartTls) {
                // Check whether STARTTLS should be required (fail if not offered)
                String starttlsRequiredEnv = System.getenv("SMTP_INTEGRATION_STARTTLS_REQUIRED");
                if (starttlsRequiredEnv == null) starttlsRequiredEnv = cfg.getProperty("SMTP_INTEGRATION_STARTTLS_REQUIRED");
                boolean starttlsRequired = "true".equalsIgnoreCase(starttlsRequiredEnv);

                if (starttlsRequired) {
                    smtpConfig = SMTPConfig.forStartTlsRequired(host, port, user, pass);
                    org.junit.jupiter.api.Assertions.assertEquals(SMTPConfig.SecurityMode.STARTTLS_REQUIRED, smtpConfig.securityMode,
                            "SMTPConfig must be in STARTTLS_REQUIRED mode for this scenario");
                } else {
                    smtpConfig = SMTPConfig.forStartTlsOptional(host, port, user, pass);
                    org.junit.jupiter.api.Assertions.assertEquals(SMTPConfig.SecurityMode.STARTTLS_OPTIONAL, smtpConfig.securityMode,
                            "SMTPConfig must be in STARTTLS_OPTIONAL mode for this scenario");
                }
            } else {
                smtpConfig = SMTPConfig.forSsl(host, port, user, pass, sslPort);
                // Ensure we are testing SSL mode explicitly
                org.junit.jupiter.api.Assertions.assertEquals(SMTPConfig.SecurityMode.SSL, smtpConfig.securityMode,
                        "SMTPConfig must be in SSL mode for this scenario");
            }

            // Enable JavaMail debug so that handshake info is printed in logs when running the test
            System.setProperty("mail.debug", "true");

            EmailModel emailModel = new EmailModel(sender, "ErmesMail Integration",
                    "Integration test - external SMTPS",
                    "Integration test message from ErmesMail (external SMTPS)");
            emailModel.addTo(recipient, "Recipient");

            EmailService emailService = new EmailService(smtpConfig, 2);
            List<String> errors = emailService.sendSynch(emailModel);
            emailService.shutdown();

            assertTrue(errors.isEmpty(), "Errors sending email via external SMTPS: " + errors.toString());
        }));

        return tests;
    }

    private java.util.Properties loadFromFiles() {
        java.util.Properties props = new java.util.Properties();
        java.nio.file.Path root = java.nio.file.Paths.get(System.getProperty("user.dir"));
        java.nio.file.Path propFile = root.resolve("smtp-integration.properties");
        java.nio.file.Path dotEnv = root.resolve(".env");
        try {
            if (java.nio.file.Files.exists(propFile)) {
                try (java.io.Reader r = java.nio.file.Files.newBufferedReader(propFile)) {
                    props.load(r);
                }
            }
        } catch (Exception e) {
            // ignore and fallback
        }

        if (java.nio.file.Files.exists(dotEnv)) {
            try (java.io.BufferedReader br = java.nio.file.Files.newBufferedReader(dotEnv)) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int idx = line.indexOf('=');
                    if (idx <= 0) continue;
                    String k = line.substring(0, idx).trim();
                    String v = line.substring(idx + 1).trim();
                    if (v.length() >= 2 && ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'")))) {
                        v = v.substring(1, v.length() - 1);
                    }
                    props.setProperty(k, v);
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return props;
    }
}
