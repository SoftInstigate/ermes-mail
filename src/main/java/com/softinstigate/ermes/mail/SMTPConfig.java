package com.softinstigate.ermes.mail;

public class SMTPConfig {

    public static final int DEFAULT_SSL_PORT = 465;

    public final String hostname;
    public final int port;
    public final String username;
    public final String password;
    public final boolean ssl;
    public final int sslPort;

    public SMTPConfig(String smtpHostname, int smtpPort, String smtpUsername, String smtpPassword, boolean sslOn, int sslPort) {
        this.hostname = smtpHostname;
        this.port = smtpPort;
        this.username = smtpUsername;
        this.password = smtpPassword;
        this.ssl = sslOn;
        this.sslPort = sslPort;
    }

    public SMTPConfig(String smtpHostname, int smtpPort, String smtpUsername, String smtpPassword, boolean sslOn) {
        this(smtpHostname, smtpPort, smtpUsername, smtpPassword, sslOn, DEFAULT_SSL_PORT); // default to standard SSL port
    }

    @Override
    public String toString() {
        return "SMTPConfig{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='************'" +
                ", ssl=" + ssl +
                ", sslPort=" + sslPort +
                '}';
    }
}