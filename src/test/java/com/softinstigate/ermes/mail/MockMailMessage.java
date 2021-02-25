package com.softinstigate.ermes.mail;

public class MockMailMessage implements MailMessage {

    public MockMailMessage() {
    }

    @Override
    public String send(String from, String to, String message) {
        return String.format("{from='%s', to='%s', message='%s'}", from, to, message);
    }
}
