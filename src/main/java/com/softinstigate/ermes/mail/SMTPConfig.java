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
 * SMTPConfig holds the SMTP server configuration, which is used by the
 * EmailService class
 */
public class SMTPConfig {

    public static final int DEFAULT_SSL_PORT = 465;

    public final String hostname;
    public final int port;
    public final String username;
    public final String password;
    public final boolean ssl;
    public final int sslPort;

    /**
     * Default constructor
     * 
     * @param smtpHostname SMTP hostname
     * @param smtpPort     SMTP port
     * @param smtpUsername SMTP username
     * @param smtpPassword SMTP password
     * @param sslOn        if true then use SSL for sending
     * @param sslPort      SSL port to use
     */
    public SMTPConfig(String smtpHostname, int smtpPort, String smtpUsername, String smtpPassword, boolean sslOn,
            int sslPort) {
        this.hostname = smtpHostname;
        this.port = smtpPort;
        this.username = smtpUsername;
        this.password = smtpPassword;
        this.ssl = sslOn;
        this.sslPort = sslPort;
    }

    /**
     * Constructor using DEFAULT_SSL_PORT
     * 
     * @param smtpHostname SMTP hostname
     * @param smtpPort     SMTP port
     * @param smtpUsername SMTP username
     * @param smtpPassword SMTP password
     * @param sslOn        if true then use SSL for sending
     */
    public SMTPConfig(String smtpHostname, int smtpPort, String smtpUsername, String smtpPassword, boolean sslOn) {
        this(smtpHostname, smtpPort, smtpUsername, smtpPassword, sslOn, DEFAULT_SSL_PORT);
    }

    @Override
    public String toString() {
        return "SMTPConfig{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", ssl=" + ssl +
                ", sslPort=" + sslPort +
                '}';
    }
}
