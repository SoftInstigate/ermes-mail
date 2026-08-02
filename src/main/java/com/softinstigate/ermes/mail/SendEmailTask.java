/*-
 * ========================LICENSE_START=================================
 * ermes-mail
 * %%
 * Copyright (C) 2021 - 2025 SoftInstigate srl
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

/**
 * Callable task to send an email via Apache Commons Email.
 */
public class SendEmailTask implements Callable<List<String>> {

    private static final Logger LOGGER = Logger.getLogger(SendEmailTask.class.getName());
    private static final AtomicBoolean MAILCAP_INITIALIZED = new AtomicBoolean(false);

    private final SMTPConfig smtpConfig;
    private final EmailModel model;
    private final String charset;
    private final HtmlEmailFactory emailFactory;

    /**
     * Default constructor
     * 
     * @param smtpConfig a SMTPConfig object
     * @param model a EmailModel object
     * @param charset a charset (default is UTF-8)
     */
    public SendEmailTask(SMTPConfig smtpConfig, EmailModel model, String charset) {
        this(smtpConfig, model, charset, new DefaultHtmlEmailFactory());
    }

    /**
     * Constructor used for tests to inject a mock HtmlEmail factory.
     */
    public SendEmailTask(SMTPConfig smtpConfig, EmailModel model, String charset, HtmlEmailFactory emailFactory) {
        this.smtpConfig = smtpConfig;
        this.model = model;
        this.charset = charset;
        this.emailFactory = emailFactory;
    }

    /**
     * Constructor with UTF-8 charset
     * 
     * @param smtpConfig a SMTPConfig object
     * @param model a EmailModel object
     */
    public SendEmailTask(SMTPConfig smtpConfig, EmailModel model) {
        this(smtpConfig, model, StandardCharsets.UTF_8.name());
    }

    /**
     * Send the EmailModel using an Apache Commons' HtmlEmail instance
     *
     * @return a {@code List<String>} of errors.
     */
    @Override
    public List<String> call() {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Processing " + model.toSecureString());
        }

        final List<String> errors = new ArrayList<>();

        try {
            ensureMailcapInitialized();

            ClassLoader original = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(EmailService.class.getClassLoader());

                HtmlEmail email = emailFactory.create();
                configureCharsetAndServer(email);
                configureSecurity(email);
                configureSenderAndContent(email);
                processAttachments(email, model, errors);
                addRecipients(email);

                if (Boolean.getBoolean("mail.debug")) {
                    email.setDebug(true);
                }

                email.send();

                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info(String.format("Email successfully sent!\nTO: %s \nCC: %s \nBCC: %s",
                            model.getToRecipients(), model.getCcRecipients(), model.getBccRecipients()));
                }

            } catch (EmailException ex) {
                LOGGER.log(Level.SEVERE, "Error sending email.", ex);
                errors.add(ex.getMessage());
            } finally {
                Thread.currentThread().setContextClassLoader(original);
            }
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error in SendEmailTask", ex);
            errors.add("Unexpected error: " + ex.getMessage());
        }

        return errors;
    }

    private void configureCharsetAndServer(HtmlEmail email) {
        email.setCharset(charset);
        email.setHostName(smtpConfig.hostname);
        email.setSmtpPort(smtpConfig.port);
        email.setAuthentication(smtpConfig.username, smtpConfig.password);
        email.setSocketConnectionTimeout(smtpConfig.connectionTimeout);
        email.setSocketTimeout(smtpConfig.socketTimeout);
    }

    private void configureSecurity(HtmlEmail email) {
        email.setSSLOnConnect(smtpConfig.ssl);
        email.setSslSmtpPort(String.valueOf(smtpConfig.sslPort));

        switch (smtpConfig.securityMode) {
            case STARTTLS_OPTIONAL -> email.setStartTLSEnabled(true);
            case STARTTLS_REQUIRED -> {
                email.setStartTLSEnabled(true);
                email.setStartTLSRequired(true);
            }
            default -> {} // PLAIN or SSL — no STARTTLS config needed
        }
    }

    private void configureSenderAndContent(HtmlEmail email) throws EmailException {
        email.setFrom(model.from, model.senderFullName);
        email.setSubject(model.subject);
        email.setHtmlMsg(model.message);
    }

    private void addRecipients(HtmlEmail email) throws EmailException {
        for (EmailModel.Recipient r : model.getToRecipients()) {
            email.addTo(r.email(), r.name());
        }
        for (EmailModel.Recipient r : model.getCcRecipients()) {
            email.addCc(r.email(), r.name());
        }
        for (EmailModel.Recipient r : model.getBccRecipients()) {
            email.addBcc(r.email(), r.name());
        }
    }

    /**
     * Attach included attachments to email
     */
    private void processAttachments(HtmlEmail email, EmailModel model, List<String> errors) {
        for (EmailModel.Attachment attachment : model.getAttachments()) {
            try {
                if (attachment.url() == null || attachment.url().isBlank()) {
                    errors.add("Attachment URL is null or blank for: " + attachment.fileName());
                    continue;
                }
                EmailAttachment emailAttachment = new EmailAttachment();
                emailAttachment.setDisposition(EmailAttachment.ATTACHMENT);
                emailAttachment.setURL(URI.create(attachment.url()).toURL());
                emailAttachment.setName(attachment.fileName());
                emailAttachment.setDescription(attachment.description());
                email.attach(emailAttachment);
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.SEVERE, String.format("Invalid attachment URI '%s'", attachment.url()), ex);
                errors.add(String.format("Invalid attachment URI '%s': %s", attachment.url(), ex.getMessage()));
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.SEVERE, String.format("Malformed attachment.url '%s'", attachment.url()), ex);
                errors.add(String.format("Malformed attachment.url '%s'", ex.getMessage()));
            } catch (EmailException ex) {
                LOGGER.log(Level.SEVERE, String.format("Error with attachment '%s'", attachment.toString()), ex);
                errors.add(String.format("Error with attachment '%s'", ex.getMessage()));
            }
        }
    }

    /**
     * Initialize MailcapCommandMap once (workaround for javax.activation MIME type resolution).
     * @see <a href="https://stackoverflow.com/a/21183987">StackOverflow reference</a>
     */
    private static void ensureMailcapInitialized() {
        if (MAILCAP_INITIALIZED.compareAndSet(false, true)) {
            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        }
    }
}
