package com.softinstigate.ermes.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MailModel {
    public final String from; // Sender's email address
    public final String senderFullName; // Sender's full name
    public final String subject;
    public final String message; // Message body
    private final List<Recipient> recipients;
    private final List<Attachment> attachments;

    public MailModel(String from, String senderFullName, String subject, String message) {
        this.from = from;
        this.senderFullName = senderFullName;
        this.subject = subject;
        this.message = message;
        this.recipients = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

    public void addRecipient(String email, String name) {
        Recipient recipient = new Recipient(email, name);
        this.recipients.add(recipient);
    }

    public void setRecipients(List<Recipient> recipients) {
        this.recipients.clear();
        this.recipients.addAll(recipients);
    }

    public void addAttachment(String url, String fileName, String description) {
        Attachment attachment = new Attachment(url, fileName, description);
        this.attachments.add(attachment);
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments.clear();
        this.attachments.addAll(attachments);
    }

    public final List<Recipient> getRecipients() {
        return Collections.unmodifiableList(recipients);
    }

    public final List<Attachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    public static class Recipient {
        public final String email;
        public final String name; // Full name

        public Recipient(String email, String name) {
            this.email = email;
            this.name = name;
        }

        @Override
        public String toString() {
            return "Recipient{" +
                    "email='" + email + '\'' +
                    ", name='" + name + '\'' +
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
                "from='" + from + '\'' +
                ", senderFullName='" + senderFullName + '\'' +
                ", subject='" + subject + '\'' +
                ", message='" + message + '\'' +
                ", recipients=" + recipients +
                ", attachments=" + attachments +
                '}';
    }
}
