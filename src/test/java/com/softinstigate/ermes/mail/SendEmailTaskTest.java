package com.softinstigate.ermes.mail;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.junit.jupiter.api.Test;

class SendEmailTaskTest {

    @Test
    void startTlsRequiredIsAppliedToEmail() throws Exception {
        HtmlEmail email = mock(HtmlEmail.class);
        HtmlEmailFactory factory = () -> email;

        SMTPConfig cfg = SMTPConfig.forStartTlsRequired("smtp", 587, "u", "p");
        EmailModel model = new EmailModel("a@b", null, "subj", "body");
        model.addTo("to@x", "To Name");

        SendEmailTask task = new SendEmailTask(cfg, model, "UTF-8", factory);

        List<String> errors = task.call();

        verify(email).setStartTLSEnabled(true);
        verify(email).setStartTLSRequired(true);
        verify(email).setFrom(eq("a@b"), any());
        verify(email).setHtmlMsg("body");
        verify(email).send();

        assertTrue(errors.isEmpty());
    }

    @Test
    void sslOnConnectIsApplied() throws Exception {
        HtmlEmail email = mock(HtmlEmail.class);
        HtmlEmailFactory factory = () -> email;

        SMTPConfig cfg = SMTPConfig.forSsl("smtp", 465, "u", "p", 465);
        EmailModel model = new EmailModel("a@b", null, "subj", "body");
        model.addTo("to@x", "To Name");

        SendEmailTask task = new SendEmailTask(cfg, model, "UTF-8", factory);
        List<String> errors = task.call();

        verify(email).setSSLOnConnect(true);
        verify(email).setSslSmtpPort("465");
        verify(email).send();
        assertTrue(errors.isEmpty());
    }

    @Test
    void plainModeDoesNotSetSslOrStartTls() throws Exception {
        HtmlEmail email = mock(HtmlEmail.class);
        HtmlEmailFactory factory = () -> email;

        SMTPConfig cfg = SMTPConfig.forPlain("smtp", 25, "u", "p");
        EmailModel model = new EmailModel("a@b", null, "subj", "body");
        model.addTo("to@x", "To Name");

        SendEmailTask task = new SendEmailTask(cfg, model, "UTF-8", factory);
        List<String> errors = task.call();

        verify(email, never()).setSSLOnConnect(true);
        verify(email, never()).setStartTLSEnabled(true);
        verify(email, never()).setStartTLSRequired(true);
        verify(email).send();
        assertTrue(errors.isEmpty());
    }

    @Test
    void startTlsOptionalSetsEnabledButNotRequired() throws Exception {
        HtmlEmail email = mock(HtmlEmail.class);
        HtmlEmailFactory factory = () -> email;

        SMTPConfig cfg = SMTPConfig.forStartTlsOptional("smtp", 587, "u", "p");
        EmailModel model = new EmailModel("a@b", null, "subj", "body");
        model.addTo("to@x", "To Name");

        SendEmailTask task = new SendEmailTask(cfg, model, "UTF-8", factory);
        List<String> errors = task.call();

        verify(email).setStartTLSEnabled(true);
        verify(email, never()).setStartTLSRequired(true);
        verify(email).send();
        assertTrue(errors.isEmpty());
    }

    @Test
    void emailExceptionIsCaughtAndAddedToErrors() throws Exception {
        HtmlEmail email = mock(HtmlEmail.class);
        doThrow(new EmailException("SMTP connection failed")).when(email).send();
        HtmlEmailFactory factory = () -> email;

        SMTPConfig cfg = SMTPConfig.forPlain("smtp", 25, "u", "p");
        EmailModel model = new EmailModel("a@b", null, "subj", "body");
        model.addTo("to@x", "To Name");

        SendEmailTask task = new SendEmailTask(cfg, model, "UTF-8", factory);
        List<String> errors = task.call();

        assertFalse(errors.isEmpty(), "Expected errors when email.send() throws EmailException");
        assertTrue(errors.get(0).contains("SMTP connection failed"));
    }

    @Test
    void processAttachmentsWithEmailExceptionAddsError() throws Exception {
        HtmlEmail email = mock(HtmlEmail.class);
        doThrow(new EmailException("attach failed")).when(email).attach(any(org.apache.commons.mail.EmailAttachment.class));
        HtmlEmailFactory factory = () -> email;

        SMTPConfig cfg = SMTPConfig.forPlain("smtp", 25, "u", "p");
        EmailModel model = new EmailModel("a@b", null, "subj", "body");
        model.addTo("to@x", "To Name");
        model.addAttachment("http://example.com/file.pdf", "file.pdf", "A file");

        SendEmailTask task = new SendEmailTask(cfg, model, "UTF-8", factory);
        List<String> errors = task.call();

        assertFalse(errors.isEmpty(), "Expected errors when attach() throws EmailException");
        assertTrue(errors.get(0).contains("attach failed"));
    }

    @Test
    void callWithCcAndBccRecipients() throws Exception {
        HtmlEmail email = mock(HtmlEmail.class);
        HtmlEmailFactory factory = () -> email;

        SMTPConfig cfg = SMTPConfig.forPlain("smtp", 25, "u", "p");
        EmailModel model = new EmailModel("a@b", null, "subj", "body");
        model.addTo("to@x", "To Name");
        model.addCc("cc@x", "CC Name");
        model.addBcc("bcc@x", "BCC Name");

        SendEmailTask task = new SendEmailTask(cfg, model, "UTF-8", factory);
        List<String> errors = task.call();

        verify(email).addTo("to@x", "To Name");
        verify(email).addCc("cc@x", "CC Name");
        verify(email).addBcc("bcc@x", "BCC Name");
        assertTrue(errors.isEmpty());
    }

    @Test
    void defaultCharsetIsUtf8() throws Exception {
        HtmlEmail email = mock(HtmlEmail.class);
        HtmlEmailFactory factory = () -> email;

        SMTPConfig cfg = SMTPConfig.forPlain("smtp", 25, "u", "p");
        EmailModel model = new EmailModel("a@b", null, "subj", "body");
        model.addTo("to@x", "To Name");

        // The 2-arg constructor delegates to this(smtpConfig, model, "UTF-8").
        // Use 4-arg with explicit "UTF-8" to verify charset is set correctly.
        SendEmailTask task = new SendEmailTask(cfg, model, "UTF-8", factory);
        List<String> errors = task.call();

        verify(email).setCharset("UTF-8");
        assertTrue(errors.isEmpty());
    }

    @Test
    void setHostNamePortAndAuthCalledCorrectly() throws Exception {
        HtmlEmail email = mock(HtmlEmail.class);
        HtmlEmailFactory factory = () -> email;

        SMTPConfig cfg = SMTPConfig.forPlain("my.smtp.host", 587, "myuser", "mypass");
        EmailModel model = new EmailModel("a@b", null, "subj", "body");
        model.addTo("to@x", "To Name");

        SendEmailTask task = new SendEmailTask(cfg, model, "UTF-8", factory);
        task.call();

        verify(email).setHostName("my.smtp.host");
        verify(email).setSmtpPort(587);
        verify(email).setAuthentication("myuser", "mypass");
    }

    @Test
    void setFromWithSenderFullName() throws Exception {
        HtmlEmail email = mock(HtmlEmail.class);
        HtmlEmailFactory factory = () -> email;

        SMTPConfig cfg = SMTPConfig.forPlain("smtp", 25, "u", "p");
        EmailModel model = new EmailModel("a@b", "Alice Sender", "subj", "body");
        model.addTo("to@x", "To Name");

        SendEmailTask task = new SendEmailTask(cfg, model, "UTF-8", factory);
        task.call();

        verify(email).setFrom("a@b", "Alice Sender");
    }

    @Test
    void setFromWithNullSenderFullName() throws Exception {
        HtmlEmail email = mock(HtmlEmail.class);
        HtmlEmailFactory factory = () -> email;

        SMTPConfig cfg = SMTPConfig.forPlain("smtp", 25, "u", "p");
        EmailModel model = new EmailModel("a@b", null, "subj", "body");
        model.addTo("to@x", "To Name");

        SendEmailTask task = new SendEmailTask(cfg, model, "UTF-8", factory);
        task.call();

        verify(email).setFrom("a@b", null);
    }
}
