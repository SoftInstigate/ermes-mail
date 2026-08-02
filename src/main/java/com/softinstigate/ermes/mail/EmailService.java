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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Email sending service that supports both asynchronous and synchronous delivery.
 *
 * <p>Delegates to {@link SendEmailTask} for the actual SMTP transaction via
 * Apache Commons Email. An internal {@link ExecutorService} thread pool is
 * created lazily on the first {@link #send(EmailModel)} call.
 *
 * <h3>Thread pool configuration</h3>
 * <ul>
 * <li>{@code threadPoolSize > 0} — the pool is created lazily; {@code send()}
 *     executes asynchronously.</li>
 * <li>{@code threadPoolSize == 0} — no pool is ever created; {@code send()}
 *     falls back to synchronous execution, returning an already-completed
 *     {@link Future}. This is useful when the caller manages concurrency
 *     externally (e.g. virtual threads).</li>
 * </ul>
 *
 * <p>Implements {@link AutoCloseable} so it can be used with try-with-resources:
 * <pre>{@code
 * try (EmailService service = new EmailService(smtpConfig)) {
 *     service.send(emailModel);
 * }
 * }</pre>
 *
 * @see #send(EmailModel)
 * @see #sendSynch(EmailModel)
 */
public class EmailService implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static final long DEFAULT_EXECUTOR_SHUTDOWN_TIMEOUT = 10; // executor shutdown timeout in seconds

    private final SMTPConfig smtpConfig;
    private final int threadPoolSize;
    private volatile ExecutorService executor; // null finché non serve

    /**
     * Constructor with default thread pool size equal to the number of available processors.
     * The thread pool is created lazily on the first {@link #send(EmailModel)} call.
     *
     * @param smtpConfig the SMTP server credentials and configuration
     * @throws NullPointerException if smtpConfig is null
     */
    public EmailService(SMTPConfig smtpConfig) {
        this(smtpConfig, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Constructor with explicit thread pool size.
     *
     * <p>The thread pool is created lazily on the first {@link #send(EmailModel)} call.
     * If {@code threadPoolSize} is 0, no pool is created and {@code send()} falls back
     * to synchronous execution, returning an already-completed {@link Future}.
     * This is useful when the caller manages concurrency externally (e.g. virtual threads).
     *
     * @param smtpConfig     the SMTP server credentials and configuration
     * @param threadPoolSize the ExecutorService thread pool size (0 = no pool, sync only)
     * @throws NullPointerException     if smtpConfig is null
     * @throws IllegalArgumentException if threadPoolSize is negative
     */
    public EmailService(SMTPConfig smtpConfig, int threadPoolSize) {
        this.smtpConfig = Objects.requireNonNull(smtpConfig, "SMTPConfig must not be null");
        if (threadPoolSize < 0) {
            throw new IllegalArgumentException("threadPoolSize must be >= 0, got: " + threadPoolSize);
        }
        this.threadPoolSize = threadPoolSize;
        if (threadPoolSize == 0) {
            LOGGER.info("EmailService initialized without thread pool: send() will execute synchronously");
        }
    }

    /**
     * Returns the internal executor, creating it lazily on first access.
     *
     * @return the ExecutorService
     * @throws IllegalStateException if threadPoolSize is 0 (no pool configured)
     */
    private synchronized ExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(threadPoolSize);
        }
        return executor;
    }

    /**
     * Send emails asynchronously, using the internal thread pool.
     *
     * <p>If no thread pool was configured ({@code threadPoolSize == 0}), this method
     * falls back to synchronous execution and returns an already-completed {@link Future}.
     *
     * @param model the email object to send
     * @return a {@code Future<List<String>>} of errors; already completed if executed synchronously
     * @throws NullPointerException if model is null
     */
    public Future<List<String>> send(EmailModel model) {
        Objects.requireNonNull(model, "EmailModel must not be null");
        if (threadPoolSize == 0) {
            return CompletableFuture.completedFuture(
                    new SendEmailTask(smtpConfig, model).call());
        }
        ExecutorService exec = getExecutor();
        if (exec.isShutdown()) {
            throw new IllegalStateException("Cannot send email: EmailService has been shut down");
        }
        Future<List<String>> errors = exec.submit(new SendEmailTask(smtpConfig, model));
        LOGGER.info("Sending emails asynchronously...");
        return errors;
    }

    /**
     * Send emails synchronously on the calling thread.
     *
     * <p>Does not use the internal thread pool regardless of configuration.
     *
     * @param model the email object to send
     * @return a {@code List<String>} of errors; empty if the email was sent successfully
     * @throws NullPointerException if model is null
     */
    public List<String> sendSynch(EmailModel model) {
        Objects.requireNonNull(model, "EmailModel must not be null");
        SendEmailTask task = new SendEmailTask(smtpConfig, model);
        return task.call();
    }

    /**
     * Shutdowns the ExecutorService.
     *
     * <p>If no thread pool was created (lazy init never triggered or {@code threadPoolSize == 0}),
     * this method is a no-op.
     *
     * @param executorShutdownTimeout timeout in seconds for executor.awaitTermination
     */
    public void shutdown(long executorShutdownTimeout) {
        if (executor == null) {
            return; // no-op: pool mai creato
        }
        executor.shutdown();
        try {
            if (executor.awaitTermination(executorShutdownTimeout, TimeUnit.SECONDS)) {
                LOGGER.info("ExecutorService terminated normally after shutdown request.");
            } else {
                LOGGER.warning("ExecutorService timeout elapsed: forcing shutdown.");
                List<Runnable> dropped = executor.shutdownNow();
                if (!dropped.isEmpty()) {
                    LOGGER.severe(dropped.size() + " tasks were abandoned and will not complete.");
                }
            }
        } catch (InterruptedException e) {
            LOGGER.warning("Shutdown interrupted; forcing immediate shutdown.");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Shutdowns the ExecutorService using the default timeout ({@value DEFAULT_EXECUTOR_SHUTDOWN_TIMEOUT}s).
     *
     * <p>If no thread pool was created, this method is a no-op.
     */
    public void shutdown() {
        this.shutdown(DEFAULT_EXECUTOR_SHUTDOWN_TIMEOUT);
    }

    /**
     * Shutdowns the executor and releases resources.
     * Equivalent to calling {@link #shutdown()}.
     */
    @Override
    public void close() {
        shutdown();
    }

}
