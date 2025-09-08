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

/**
 * SMTPConfig holds the SMTP server configuration used by {@link EmailService}.
 *
 * <p>
 * Construction is done via explicit factory methods to make the security
 * intent clear. Use one of:
 * <ul>
 * <li>{@link #forPlain(String,int,String,String)}</li>
 * <li>{@link #forSsl(String,int,String,String,int)}</li>
 * <li>{@link #forStartTlsOptional(String,int,String,String)}</li>
 * <li>{@link #forStartTlsRequired(String,int,String,String)}</li>
 * </ul>
 *
 * <p>
 * The {@link SecurityMode} enum expresses the security policy (plain, SSL,
 * STARTTLS optional or required).
 */
public class SMTPConfig {

    public static final int DEFAULT_SSL_PORT = 465;

    public final String hostname;
    public final int port;
    public final String username;
    public final String password;
    public final boolean ssl;
    public final int sslPort;
    public final SecurityMode securityMode;

    /**
     * Security mode for the SMTP connection.
     */
    public enum SecurityMode {
        PLAIN, SSL, STARTTLS_OPTIONAL, STARTTLS_REQUIRED
    }

    /**
     * Private initializer used by factory methods.
     *
     * @param smtpHostname SMTP hostname
     * @param smtpPort SMTP port
     * @param smtpUsername SMTP username
     * @param smtpPassword SMTP password
     * @param sslOn whether SSL-on-connect should be used
     * @param sslPort SSL port value (only meaningful for SSL mode)
     * @param mode the desired {@link SecurityMode}
     */
    private SMTPConfig(String smtpHostname, int smtpPort, String smtpUsername, String smtpPassword, boolean sslOn,
            int sslPort, SecurityMode mode) {
        this.hostname = smtpHostname;
        this.port = smtpPort;
        this.username = smtpUsername;
        this.password = smtpPassword;
        this.ssl = sslOn;
        this.sslPort = sslPort;
        this.securityMode = mode;
    }

    /**
     * Create a plain (no TLS) SMTP configuration.
     */
    public static SMTPConfig forPlain(String smtpHostname, int smtpPort, String smtpUsername, String smtpPassword) {
        return new SMTPConfig(smtpHostname, smtpPort, smtpUsername, smtpPassword, false, DEFAULT_SSL_PORT,
                SecurityMode.PLAIN);
    }

    /**
     * Create an SSL-on-connect SMTP configuration (implicit TLS, typically port 465).
     */
    public static SMTPConfig forSsl(String smtpHostname, int smtpPort, String smtpUsername, String smtpPassword,
            int sslPort) {
        return new SMTPConfig(smtpHostname, smtpPort, smtpUsername, smtpPassword, true, sslPort,
                SecurityMode.SSL);
    }

    /**
     * Create a STARTTLS (opportunistic) SMTP configuration: upgrade to TLS if
     * the server supports it, otherwise continue in plaintext.
     */
    public static SMTPConfig forStartTlsOptional(String smtpHostname, int smtpPort, String smtpUsername,
            String smtpPassword) {
        return new SMTPConfig(smtpHostname, smtpPort, smtpUsername, smtpPassword, false, DEFAULT_SSL_PORT,
                SecurityMode.STARTTLS_OPTIONAL);
    }

    /**
     * Create a STARTTLS-required SMTP configuration: fail if the server does not
     * advertise STARTTLS.
     */
    public static SMTPConfig forStartTlsRequired(String smtpHostname, int smtpPort, String smtpUsername,
            String smtpPassword) {
        return new SMTPConfig(smtpHostname, smtpPort, smtpUsername, smtpPassword, false, DEFAULT_SSL_PORT,
                SecurityMode.STARTTLS_REQUIRED);
    }

    @Override
    public String toString() {
        return "SMTPConfig{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", securityMode=" + securityMode +
                ", sslPort=" + sslPort +
                '}';
    }
}
