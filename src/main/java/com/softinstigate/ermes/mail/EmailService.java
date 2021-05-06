package com.softinstigate.ermes.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;

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
        // Add explicit MailcapCommandMap (See https://stackoverflow.com/a/25650033)
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");
        LOGGER.info("MailService initialized with {}", smtpConfig.toString());
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
