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
     * @return a Future<List<String>> of errors. If the list is empty then no errors!
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
     * @return a List<String> of errors. If the list is empty then no errors!
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
