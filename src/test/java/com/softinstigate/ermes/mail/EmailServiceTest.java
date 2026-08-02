/*-
 * ========================LICENSE_START=================================
 * ermes-mail
 * %%
 * Copyright (C) 2021 - 2026 SoftInstigate srl
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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

class EmailServiceTest {

    private static final SMTPConfig DUMMY_CONFIG = SMTPConfig.forPlain("localhost", 25, "", "");

    @Test
    void constructorRejectsNullSmtpConfig() {
        assertThrows(NullPointerException.class, () -> new EmailService(null, 1));
    }

    @Test
    void constructorWithZeroPoolSize() {
        assertDoesNotThrow(() -> new EmailService(DUMMY_CONFIG, 0));
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
    void sendRejectsNullModel() {
        EmailService service = new EmailService(DUMMY_CONFIG, 1);
        try {
            assertThrows(NullPointerException.class, () -> service.send(null));
        } finally {
            service.shutdown();
        }
    }

    @Test
    void sendReturnsFutureThatCompletes() throws Exception {
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
    void sendAfterShutdownThrowsIllegalState() throws Exception {
        EmailService service = new EmailService(DUMMY_CONFIG, 1);
        // Trigger lazy executor creation by calling send() once
        EmailModel model = new EmailModel("a@b.com", null, "Test", "Body");
        model.addTo("x@y.com", "X");
        service.send(model).get(); // creates the executor
        service.shutdown();

        assertThrows(IllegalStateException.class, () -> service.send(model));
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

    // --- Tests for threadPoolSize = 0 ---

    @Test
    void sendSynchWithZeroPoolSize() {
        SMTPConfig cfg = SMTPConfig.forPlain("localhost", 19999, "", "");
        EmailService service = new EmailService(cfg, 0);
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
    void sendWithZeroPoolSizeReturnsCompletedFuture() throws Exception {
        SMTPConfig cfg = SMTPConfig.forPlain("localhost", 19999, "", "");
        EmailService service = new EmailService(cfg, 0);
        try {
            EmailModel model = new EmailModel("a@b.com", null, "Test", "Body");
            model.addTo("x@y.com", "X");

            Future<List<String>> future = service.send(model);
            assertNotNull(future);
            assertTrue(future.isDone(), "Future should already be completed with poolSize=0");

            List<String> errors = future.get();
            assertNotNull(errors);
            assertFalse(errors.isEmpty(), "Expected connection errors for unreachable port");
        } finally {
            service.shutdown();
        }
    }

    @Test
    void shutdownWithZeroPoolSizeIsNoOp() {
        EmailService service = new EmailService(DUMMY_CONFIG, 0);
        assertDoesNotThrow(() -> service.shutdown());
    }

    @Test
    void sendAfterShutdownWithZeroPoolSizeStillWorks() {
        SMTPConfig cfg = SMTPConfig.forPlain("localhost", 19999, "", "");
        EmailService service = new EmailService(cfg, 0);
        service.shutdown(); // no-op

        EmailModel model = new EmailModel("a@b.com", null, "Test", "Body");
        model.addTo("x@y.com", "X");

        // send() should still work because there's no executor to be shut down
        Future<List<String>> future = service.send(model);
        assertNotNull(future);
        assertTrue(future.isDone());
    }

    // --- Tests for lazy executor ---

    @Test
    void shutdownWithoutPriorSendIsNoOp() {
        EmailService service = new EmailService(DUMMY_CONFIG, 4);
        // Never called send() — executor should not exist
        assertDoesNotThrow(() -> service.shutdown());
    }
}
