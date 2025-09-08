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

import java.util.logging.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Receive a SMTP server configuration and send emails with HTML content
 */
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static final long DEFAULT_EXECUTOR_SHUTDOWN_TIMEOUT = 10; // executor shutdown timeout in seconds

    private final SMTPConfig smtpConfig;
    private final ExecutorService executor;

    /**
     * Constructor
     *
     * @param smtpConfig     the SMTP server credentials and configuration
     * @param threadPoolSize the ExecutorService thread poll size
     */
    public EmailService(SMTPConfig smtpConfig, int threadPoolSize) {
        this.smtpConfig = smtpConfig;
        executor = Executors.newFixedThreadPool(threadPoolSize);
        LOGGER.info("MailService initialized with " + smtpConfig.toString());
    }

    /**
     * Send emails asynchronously, using the ExecutorService
     *
     * @param model the email object to send
     * @return a {@code Future<List<String>> } of errors.
     */
    public Future<List<String>> send(EmailModel model) {
        Future<List<String>> errors = executor.submit(new SendEmailTask(smtpConfig, model));
        LOGGER.info("Sending emails asynchronously...");
        return errors;
    }

    /**
     * Send emails synchronously
     *
     * @param model the email object to send
     * @return a {@code List<String>} of errors.
     */
    public List<String> sendSynch(EmailModel model) {
        SendEmailTask task = new SendEmailTask(smtpConfig, model);
        return task.call();
    }

    /**
     * Shutdowns the ExecutorService
     *
     * @param executorShutdownTimeout timeout for executor.awaitTermination method
     */
    public void shutdown(long executorShutdownTimeout) {
        executor.shutdown();
        try {
            if (executor.awaitTermination(executorShutdownTimeout, TimeUnit.SECONDS)) {
                LOGGER.info("ExecutorService terminated normally after shutdown request.");
            } else {
                LOGGER.warning("ExecutorService timeout elapsed: some emails may not have been sent.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Shutdowns the ExecutorService using DEFAULT_EXECUTOR_SHUTDOWN_TIMEOUT
     */
    public void shutdown() {
        this.shutdown(DEFAULT_EXECUTOR_SHUTDOWN_TIMEOUT);
    }

}
