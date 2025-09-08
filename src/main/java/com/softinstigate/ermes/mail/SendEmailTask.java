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

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

/**
 * Runnable class to invoke the email.send() method in a thread
 */
public class SendEmailTask implements Callable<List<String>> {

    private static final Logger LOGGER = Logger.getLogger(SendEmailTask.class.getName());

    private final SMTPConfig smtpConfig;
    private final EmailModel model;
    private final String charset;

    /**
     * Default constructor
     * 
     * @param smtpConfig a SMTPConfig object
     * @param model a EmailModel object
     * @param charset a charset (default is UTF-8)
     */
    public SendEmailTask(SMTPConfig smtpConfig, EmailModel model, String charset) {
        this.smtpConfig = smtpConfig;
        this.model = model;
        this.charset = charset;
    }

    /**
     * Constructor with UTF-8 charset
     * 
     * @param smtpConfig a SMTPConfig object
     * @param model a EmailModel object
     */
    public SendEmailTask(SMTPConfig smtpConfig, EmailModel model) {
        this(smtpConfig, model, "UTF-8");
    }

    /**
     * Send the EmailModel using an Apache Commons' HtmlEmail instance
     *
     * @return a {@code Future<List<String>>} of errors.
     */
    @Override
    public List<String> call() {
        LOGGER.info("Processing " + model.toString());

        final List<String> errors = new ArrayList<>();

        // Begin FIX for javax.activation.UnsupportedDataTypeException: no object DCH
        // for MIME type multipart/alternative;
        setDefaultCommandMap();
        Thread.currentThread().setContextClassLoader(EmailService.class.getClassLoader());
        // End Fix

        HtmlEmail email = new HtmlEmail();
        try {
            email.setCharset(charset);
            email.setHostName(smtpConfig.hostname);
            email.setSmtpPort(smtpConfig.port);
            email.setAuthentication(smtpConfig.username, smtpConfig.password);
            email.setSSLOnConnect(smtpConfig.ssl);
            email.setSslSmtpPort(String.valueOf(smtpConfig.sslPort));
            email.setFrom(model.from, model.senderFullName);
            email.setSubject(model.subject);
            email.setHtmlMsg(model.message);

            processAttachments(email, model, errors);

            for (EmailModel.Recipient r : model.getToRecipients()) {
                email.addTo(r.email, r.name);
            }

            for (EmailModel.Recipient r : model.getCcRecipients()) {
                email.addCc(r.email, r.name);
            }

            for (EmailModel.Recipient r : model.getBccRecipients()) {
                email.addBcc(r.email, r.name);
            }

            email.send();

            LOGGER.info(String.format("Email successfully sent!\nTO: %s \nCC: %s \nBCC: %s", model.getToRecipients(),
                    model.getCcRecipients(), model.getBccRecipients()));

        } catch (EmailException ex) {
            LOGGER.log(Level.SEVERE, "Error sending email.", ex);
            errors.add(ex.getMessage());
        }

        return errors;
    }

    /**
     * Attach included attachments to email
     *
     * @param email a HtmlEmail instance
     * @param model the EmailModel to process
     */
    private void processAttachments(HtmlEmail email, EmailModel model, List<String> errors) {
        for (EmailModel.Attachment attachment : model.getAttachments()) {
            try {
                EmailAttachment emailAttachment = new EmailAttachment();
                emailAttachment.setDisposition(EmailAttachment.ATTACHMENT);
                emailAttachment.setURL(URI.create(attachment.url).toURL());
                emailAttachment.setName(attachment.fileName);
                emailAttachment.setDescription(attachment.description);
                email.attach(emailAttachment);
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.SEVERE, String.format("Malformed attachment.url '%s'", attachment.url), ex);
                errors.add(String.format("Malformed attachment.url '%s'", ex.getMessage()));
            } catch (EmailException ex) {
                LOGGER.log(Level.SEVERE, String.format("Error with attachment '%s'", attachment.toString()), ex);
                errors.add(String.format("Error with attachment '%s'", ex.getMessage()));
            }
        }
    }

    /**
     * Add explicit MailcapCommandMap (workaround - see https://stackoverflow.com/a/21183987)
     */
    private void setDefaultCommandMap() {
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
    }
}
