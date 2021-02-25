package com.softinstigate.ermes.mail;

public interface MailMessage {
    String send(String from, String to, String message);
}
