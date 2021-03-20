package com.softinstigate.ermes.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable email object model
 */
public class EmailModel {
    public final String from; // Sender's email address
    public final String senderFullName; // Sender's full name
    public final String subject;
    public final String message; // Message body
    private final List<Recipient> recipients;
    private final List<Attachment> attachments;

    /**
     * Copnstructor
     *
     * @param from           sender
     * @param senderFullName sender full name
     * @param subject        subject of the email
     * @param message        message body
     */
    public EmailModel(String from, String senderFullName, String subject, String message) {
        this.from = from;
        this.senderFullName = senderFullName;
        this.subject = subject;
        this.message = message;
        this.recipients = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

    /**
     * adds a recipient
     *
     * @param email recipient's email address
     * @param name  recipient's full name
     */
    public void addRecipient(String email, String name) {
        Recipient recipient = new Recipient(email, name);
        this.recipients.add(recipient);
    }

    /**
     * Replace the list of recipients
     *
     * @param recipients the new list of recipients
     */
    public void setRecipients(List<Recipient> recipients) {
        this.recipients.clear();
        this.recipients.addAll(recipients);
    }

    /**
     * Add an attachment
     *
     * @param url         the attachment's url
     * @param fileName    the attachment's filename
     * @param description a description
     */
    public void addAttachment(String url, String fileName, String description) {
        Attachment attachment = new Attachment(url, fileName, description);
        this.attachments.add(attachment);
    }

    /**
     * Replace the list of attachments
     *
     * @param attachments the new list of attachments
     */
    public void setAttachments(List<Attachment> attachments) {
        this.attachments.clear();
        this.attachments.addAll(attachments);
    }

    /**
     * @return the list of recipients
     */
    public final List<Recipient> getRecipients() {
        return Collections.unmodifiableList(recipients);
    }

    /**
     * @return the list of attachments
     */
    public final List<Attachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    /**
     * Immutable recipient object model
     */
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

    /**
     * Immutable attachment object model
     */
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
