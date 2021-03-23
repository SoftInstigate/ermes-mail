package com.softinstigate.ermes.mail;

import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Receive a SMTP server configuration and send emails with HTML content
 */
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
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
        LOGGER.info("MailService initialized with {}", smtpConfig.toString());
    }

    /**
     * Send emails asynchronously, using the ExecutorService
     *
     * @param model the email object to send
     */
    public void send(EmailModel model) {
        executor.execute(new SendEmailTask(smtpConfig, model));
        LOGGER.info("Sending emails asynchronously...");
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
                LOGGER.warn("ExecutorService timeout elapsed: some emails may not have been sent.");
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
