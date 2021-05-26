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
    private final List<Recipient> to;
    private final List<Recipient> cc;
    private final List<Recipient> bcc;
    private final List<Attachment> attachments;

    /**
     * Constructor
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
        this.to = new ArrayList<>();
        this.cc = new ArrayList<>();
        this.bcc = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

    /**
     * adds a to recipient
     *
     * @param email recipient's email address
     * @param name  recipient's full name
     */
    public void addTo(String email, String name) {
        this.to.add(new Recipient(email, name));
    }

    /**
     * adds a Cc recipient
     *
     * @param email cc's email address
     * @param name  cc's full name
     */
    public void addCc(String email, String name) {
        this.cc.add(new Recipient(email, name));
    }

    /**
     * adds a Bcc recipient
     *
     * @param email bcc's email address
     * @param name  bcc's full name
     */
    public void addBcc(String email, String name) {
        this.bcc.add(new Recipient(email, name));
    }

    /**
     * Replace the list of recipients
     *
     * @param recipients the new list of recipients
     */
    public void setTo(List<Recipient> recipients) {
        this.to.clear();
        this.to.addAll(recipients);
    }

    /**
     * Replace the list of Cc
     *
     * @param recipients the new list of Cc recipients
     */
    public void setCc(List<Recipient> recipients) {
        this.cc.clear();
        this.cc.addAll(recipients);
    }

    /**
     * Replace the list of Bcc
     *
     * @param recipients the new list of Bcc recipients
     */
    public void setBcc(List<Recipient> recipients) {
        this.bcc.clear();
        this.bcc.addAll(recipients);
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
     * @return the list of to recipients
     */
    public final List<Recipient> getToRecipients() {
        return Collections.unmodifiableList(to);
    }

    /**
     * @return the list of to recipients
     */
    public final List<Recipient> getCcRecipients() {
        return Collections.unmodifiableList(cc);
    }

    /**
     * @return the list of to recipients
     */
    public final List<Recipient> getBccRecipients() {
        return Collections.unmodifiableList(bcc);
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
                ", to=" + to +
                ", cc=" + cc +
                ", bcc=" + bcc +
                ", attachments=" + attachments +
                '}';
    }
}
