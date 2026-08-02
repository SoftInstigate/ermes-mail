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
 * Immutable SMTP server configuration used by {@link EmailService}.
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
 * STARTTLS optional or required). Connection and socket timeouts default to
 * {@value #DEFAULT_CONNECTION_TIMEOUT} ms and {@value #DEFAULT_SOCKET_TIMEOUT} ms
 * respectively.
 */
public class SMTPConfig {

    /** Default SSL port for SMTPS (implicit TLS). */
    public static final int DEFAULT_SSL_PORT = 465;

    /** Default connection timeout in milliseconds. */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 10_000;

    /** Default socket (read) timeout in milliseconds. */
    public static final int DEFAULT_SOCKET_TIMEOUT = 60_000;

    /** SMTP server hostname. */
    public final String hostname;

    /** SMTP server port. */
    public final int port;

    /** SMTP username for authentication ({@code null} or empty for no auth). */
    public final String username;

    /** SMTP password for authentication. */
    public final String password;

    /** Whether SSL-on-connect (implicit TLS) is enabled. */
    public final boolean ssl;

    /** SSL port value (only meaningful when {@link #ssl} is {@code true}). */
    public final int sslPort;

    /** The security mode for this configuration. */
    public final SecurityMode securityMode;

    /** Socket connection timeout in milliseconds. */
    public final int connectionTimeout;

    /** Socket read timeout in milliseconds. */
    public final int socketTimeout;

    /**
     * Security mode for the SMTP connection.
     */
    public enum SecurityMode {
        /** No encryption. */
        PLAIN,
        /** Implicit TLS/SSL (SMTPS, typically port 465). */
        SSL,
        /** Opportunistic STARTTLS: upgrade if available, otherwise continue in plaintext. */
        STARTTLS_OPTIONAL,
        /** Mandatory STARTTLS: fail if the server does not advertise STARTTLS. */
        STARTTLS_REQUIRED
    }

    /**
     * Private initializer used by factory methods.
     */
    private SMTPConfig(String smtpHostname, int smtpPort, String smtpUsername, String smtpPassword, boolean sslOn,
            int sslPort, SecurityMode mode, int connectionTimeout, int socketTimeout) {
        if (smtpHostname == null || smtpHostname.isBlank()) {
            throw new IllegalArgumentException("SMTP hostname must not be null or blank");
        }
        if (smtpPort < 1 || smtpPort > 65535) {
            throw new IllegalArgumentException("SMTP port must be between 1 and 65535, got: " + smtpPort);
        }
        this.hostname = smtpHostname;
        this.port = smtpPort;
        this.username = smtpUsername;
        this.password = smtpPassword;
        this.ssl = sslOn;
        this.sslPort = sslPort;
        this.securityMode = mode;
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
    }

    /**
     * Create a plain (no TLS) SMTP configuration.
     *
     * @param smtpHostname SMTP server hostname (must not be null or blank)
     * @param smtpPort     SMTP server port (1–65535)
     * @param smtpUsername SMTP username ({@code null} or empty for no auth)
     * @param smtpPassword SMTP password
     * @return a new {@code SMTPConfig} with {@link SecurityMode#PLAIN}
     */
    public static SMTPConfig forPlain(String smtpHostname, int smtpPort, String smtpUsername, String smtpPassword) {
        return new SMTPConfig(smtpHostname, smtpPort, smtpUsername, smtpPassword, false, DEFAULT_SSL_PORT,
                SecurityMode.PLAIN, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Create an SSL-on-connect SMTP configuration (implicit TLS, typically port 465).
     *
     * @param smtpHostname SMTP server hostname (must not be null or blank)
     * @param smtpPort     SMTP server port (1–65535)
     * @param smtpUsername SMTP username ({@code null} or empty for no auth)
     * @param smtpPassword SMTP password
     * @param sslPort      the SSL port to use (e.g. 465)
     * @return a new {@code SMTPConfig} with {@link SecurityMode#SSL}
     */
    public static SMTPConfig forSsl(String smtpHostname, int smtpPort, String smtpUsername, String smtpPassword,
            int sslPort) {
        return new SMTPConfig(smtpHostname, smtpPort, smtpUsername, smtpPassword, true, sslPort,
                SecurityMode.SSL, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Create a STARTTLS (opportunistic) SMTP configuration: upgrade to TLS if
     * the server supports it, otherwise continue in plaintext.
     *
     * @param smtpHostname SMTP server hostname (must not be null or blank)
     * @param smtpPort     SMTP server port (1–65535)
     * @param smtpUsername SMTP username ({@code null} or empty for no auth)
     * @param smtpPassword SMTP password
     * @return a new {@code SMTPConfig} with {@link SecurityMode#STARTTLS_OPTIONAL}
     */
    public static SMTPConfig forStartTlsOptional(String smtpHostname, int smtpPort, String smtpUsername,
            String smtpPassword) {
        return new SMTPConfig(smtpHostname, smtpPort, smtpUsername, smtpPassword, false, DEFAULT_SSL_PORT,
                SecurityMode.STARTTLS_OPTIONAL, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Create a STARTTLS-required SMTP configuration: fail if the server does not
     * advertise STARTTLS.
     *
     * @param smtpHostname SMTP server hostname (must not be null or blank)
     * @param smtpPort     SMTP server port (1–65535)
     * @param smtpUsername SMTP username ({@code null} or empty for no auth)
     * @param smtpPassword SMTP password
     * @return a new {@code SMTPConfig} with {@link SecurityMode#STARTTLS_REQUIRED}
     */
    public static SMTPConfig forStartTlsRequired(String smtpHostname, int smtpPort, String smtpUsername,
            String smtpPassword) {
        return new SMTPConfig(smtpHostname, smtpPort, smtpUsername, smtpPassword, false, DEFAULT_SSL_PORT,
                SecurityMode.STARTTLS_REQUIRED, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
    }

    @Override
    public String toString() {
        return "SMTPConfig{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", username='" + (username != null && !username.isEmpty() ? "[REDACTED]" : "null") + '\'' +
                ", securityMode=" + securityMode +
                ", sslPort=" + sslPort +
                '}';
    }

    /**
     * Create a secure string representation for logging purposes.
     * This excludes sensitive authentication information.
     * 
     * @return a sanitized string representation
     */
    public String toSecureString() {
        return "SMTPConfig{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", hasCredentials=" + (username != null && !username.isEmpty()) +
                ", securityMode=" + securityMode +
                ", sslPort=" + sslPort +
                '}';
    }
}
