package com.softinstigate.ermes.mail;

public class SMTPConfig {
    public final String hostname;
    public final int port;
    public final String username;
    public final String password;
    public final boolean ssl;
    public final String sslPort;

    public SMTPConfig(String smtpHostname, int smtpPort, String smtpUsername, String smtpPassword, boolean sslOn, String sslPort) {
        this.hostname = smtpHostname;
        this.port = smtpPort;
        this.username = smtpUsername;
        this.password = smtpPassword;
        this.ssl = sslOn;
        this.sslPort = sslPort;
    }

    public SMTPConfig(String smtpHostname, int smtpPort, String smtpUsername, String smtpPassword, boolean sslOn) {
        this(smtpHostname,smtpPort,smtpUsername,smtpPassword,sslOn,"465"); // default to standard SSL port
    }

    @Override
    public String toString() {
        return "SMTPConfig{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='************'" +
                ", ssl=" + ssl +
                ", sslPort='" + sslPort + '\'' +
                '}';
    }
}