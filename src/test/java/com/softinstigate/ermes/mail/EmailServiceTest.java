package com.softinstigate.ermes.mail;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.junit.jupiter.api.Test;

class EmailServiceTest {

    private static final SMTPConfig DUMMY_CONFIG = SMTPConfig.forPlain("localhost", 25, "", "");

    @Test
    void constructorRejectsNullSmtpConfig() {
        assertThrows(NullPointerException.class, () -> new EmailService(null, 1));
    }

    @Test
    void constructorRejectsZeroThreadPool() {
        assertThrows(IllegalArgumentException.class, () -> new EmailService(DUMMY_CONFIG, 0));
    }

    @Test
    void constructorRejectsNegativeThreadPool() {
        assertThrows(IllegalArgumentException.class, () -> new EmailService(DUMMY_CONFIG, -1));
    }

    @Test
    void sendSynchRejectsNullModel() {
        EmailService service = new EmailService(DUMMY_CONFIG, 1);
        try {
            assertThrows(NullPointerException.class, () -> service.sendSynch(null));
        } finally {
            service.shutdown();
        }
    }

    @Test
    void sendReturnsFutureThatCompletes() throws Exception {
        // Use localhost on a port not listening — connection refused is immediate
        SMTPConfig cfg = SMTPConfig.forPlain("localhost", 19999, "", "");
        EmailService service = new EmailService(cfg, 1);
        try {
            EmailModel model = new EmailModel("a@b.com", null, "Test", "Body");
            model.addTo("x@y.com", "X");

            Future<List<String>> future = service.send(model);
            assertNotNull(future, "send() should return a non-null Future");

            List<String> errors = future.get();
            assertNotNull(errors, "Future should resolve to a non-null list");
            assertFalse(errors.isEmpty(), "Expected connection errors for unreachable port");
        } finally {
            service.shutdown();
        }
    }

    @Test
    void sendSynchReturnsErrorsForUnreachablePort() {
        SMTPConfig cfg = SMTPConfig.forPlain("localhost", 19999, "", "");
        EmailService service = new EmailService(cfg, 1);
        try {
            EmailModel model = new EmailModel("a@b.com", null, "Test", "Body");
            model.addTo("x@y.com", "X");

            List<String> errors = service.sendSynch(model);
            assertNotNull(errors);
            assertFalse(errors.isEmpty(), "Expected connection errors for unreachable port");
        } finally {
            service.shutdown();
        }
    }

    @Test
    void shutdownTerminatesExecutor() {
        EmailService service = new EmailService(DUMMY_CONFIG, 1);
        service.shutdown();

        EmailModel model = new EmailModel("a@b.com", null, "Test", "Body");
        model.addTo("x@y.com", "X");

        assertThrows(RejectedExecutionException.class, () -> service.send(model));
    }

    @Test
    void shutdownWithCustomTimeout() {
        EmailService service = new EmailService(DUMMY_CONFIG, 1);
        assertDoesNotThrow(() -> service.shutdown(5));
    }

    @Test
    void shutdownAfterSendWaitsForCompletion() throws Exception {
        SMTPConfig cfg = SMTPConfig.forPlain("localhost", 19999, "", "");
        EmailService service = new EmailService(cfg, 1);

        EmailModel model = new EmailModel("a@b.com", null, "Test", "Body");
        model.addTo("x@y.com", "X");

        Future<List<String>> future = service.send(model);
        service.shutdown(10);

        List<String> errors = future.get();
        assertNotNull(errors);
    }
}
