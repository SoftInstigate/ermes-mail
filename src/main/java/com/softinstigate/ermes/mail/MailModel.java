package com.softinstigate.ermes.mail;

import java.util.ArrayList;
import java.util.List;

public class MailModel {
    public final String message; // Message body
    public final String from; // Sender's email
    public final String sender; // Sender's full name
    public final String replyTo;
    private final List<Recipient> recipients;
    private final List<Attachment> attachments;

    public MailModel(String message, String from, String sender, String replyTo) {
        this.message = message;
        this.from = from;
        this.sender = sender;
        this.replyTo = replyTo;
        this.recipients = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

    public void addRecipient(String email, String name, String subject) {
        Recipient recipient = new Recipient(email, name, subject);
        this.recipients.add(recipient);
    }

    public void addAttachment(String url, String fileName, String description) {
        Attachment attachment = new Attachment(url, fileName, description);
        this.attachments.add(attachment);
    }

    public List<Recipient> getRecipients() {
        return recipients;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public static class Recipient {
        public final String email;
        public final String name; // Full name
        public final String subject;

        public Recipient(String email, String name, String subject) {
            this.email = email;
            this.name = name;
            this.subject = subject;
        }

        @Override
        public String toString() {
            return "Recipient{" +
                    "email='" + email + '\'' +
                    ", name='" + name + '\'' +
                    ", subject='" + subject + '\'' +
                    '}';
        }
    }

    public static class Attachment {
        public final String url;
        public final String fileName;
        public final String description;

        public Attachment(String url, String fileName, String description) {
            this.url = url;
            this.fileName = fileName;
            this.description = description;
        }

        @Override
        public String toString() {
            return "Attachment{" +
                    "url='" + url + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "MailModel{" +
                "message='" + message + '\'' +
                ", from='" + from + '\'' +
                ", sender='" + sender + '\'' +
                ", replyTo='" + replyTo + '\'' +
                ", recipients=" + recipients +
                ", attachments=" + attachments +
                '}';
    }
}
