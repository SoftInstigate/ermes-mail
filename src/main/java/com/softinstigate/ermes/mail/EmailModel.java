/*-
 * ========================LICENSE_START=================================
 * ermes-mail
 * %%
 * Copyright (C) 2021 - 2022 SoftInstigate srl
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * E-mails object model
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
     * @param from           sender e-mail address
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
     * adds a TO recipient
     *
     * @param email recipient email address
     * @param name  recipient full name
     */
    public void addTo(String email, String name) {
        this.to.add(new Recipient(email, name));
    }

    /**
     * adds a list of TO recipient
     *
     * @param emails list of TO email addresses
     */
    public void setMultipleTo(List<String> emails) {
        List<Recipient> recipients = new ArrayList<Recipient>(emails.size());
        emails.forEach(e -> {
            recipients.add(new Recipient(e, null));
        });
        this.setTo(recipients);
    }

    /**
     * adds a CC recipient
     *
     * @param email CC email address
     * @param name  CC full name
     */
    public void addCc(String email, String name) {
        this.cc.add(new Recipient(email, name));
    }

    /**
     * adds a list of CC recipients
     *
     * @param emails list of multiple CC email addresses
     */
    public void setMultipleCc(List<String> emails) {
        List<Recipient> recipients = new ArrayList<Recipient>(emails.size());
        emails.forEach(e -> {
            recipients.add(new Recipient(e, null));
        });
        this.setCc(recipients);
    }

    /**
     * adds a BCC recipient
     *
     * @param email BCC email address
     * @param name  BCC full name
     */
    public void addBcc(String email, String name) {
        this.bcc.add(new Recipient(email, name));
    }

    /**
     * adds a list of BCC recipients
     *
     * @param emails list of multiple BCC email addresses
     */
    public void setMultipleBcc(List<String> emails) {
        List<Recipient> recipients = new ArrayList<Recipient>(emails.size());
        emails.forEach(e -> {
            recipients.add(new Recipient(e, null));
        });
        this.setBcc(recipients);
    }

    /**
     * Replace the list of TO recipients
     *
     * @param recipients the new list of TO recipients
     */
    public void setTo(List<Recipient> recipients) {
        this.to.clear();
        this.to.addAll(recipients);
    }

    /**
     * Replace the list of CC recipients
     *
     * @param recipients the new list of CC recipients
     */
    public void setCc(List<Recipient> recipients) {
        this.cc.clear();
        this.cc.addAll(recipients);
    }

    /**
     * Replace the list of BCC recipients
     *
     * @param recipients the new list of BCC recipients
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
     * get all TO recipients
     * 
     * @return the list of TO recipients
     */
    public final List<Recipient> getToRecipients() {
        return Collections.unmodifiableList(to);
    }

    /**
     * get all CC recipients
     * 
     * @return the list of CC recipients
     */
    public final List<Recipient> getCcRecipients() {
        return Collections.unmodifiableList(cc);
    }

    /**
     * get all BCC recipients
     * 
     * @return the list of BCC recipients
     */
    public final List<Recipient> getBccRecipients() {
        return Collections.unmodifiableList(bcc);
    }

    /**
     * get all attachments
     * 
     * @return the list of attachments
     */
    public final List<Attachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    /**
     * Recipient object model
     */
    public static class Recipient {
        public final String email;
        public final String name; // Full name

        /**
         * Default constructor
         * 
         * @param email recipients' email
         * @param name  optional recipient's name (cab be null)
         */
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
     * Attachment object model
     */
    public static class Attachment {
        public final String url;
        public final String fileName;
        public final String description;

        /**
         * Default constructor
         * 
         * @param url           attachment's URL
         * @param fileName      file to attach
         * @param description   file description
         */
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
