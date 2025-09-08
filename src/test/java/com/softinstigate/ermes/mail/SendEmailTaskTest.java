package com.softinstigate.ermes.mail;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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
}
