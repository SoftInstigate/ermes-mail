/*-
 * ========================LICENSE_START=================================
 * ermes-mail
 * %%
 * Copyright (C) 2021 - 2022 SoftInstigate srl
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
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Implements the command line interface with picocli
 * Build it with {@code mvn clean package} and the run with
 * {@code java -jar target/ermes-mail.jar --help}
 */
@CommandLine.Command(
    name = "java -jar ermes-mail.jar",
    description = "Sends an HTML email to the given recipient(s).",
    footer = "Copyright(c) 2022 SoftInstigate srl (https://www.softinstigate.com)",
    sortOptions = false,
    versionProvider = com.softinstigate.ermes.mail.VersionProvider.class)
public class Main implements Callable<Integer> {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    @CommandLine.Option(names = { "-h", "--host" }, defaultValue = "localhost", description = "SMTP host.")
    private String smtpHost;

    @CommandLine.Option(names = { "-p", "--port" }, defaultValue = "25", description = "SMTP port.")
    private int smtpPort;

    @CommandLine.Option(names = { "-u", "--user" }, defaultValue = "", description = "SMTP user name.")
    private String user;

    @CommandLine.Option(names = { "-P", "--password" }, defaultValue = "",
        arity = "0..1", interactive = true, description = "SMTP user password.")
    private String password;

    @CommandLine.Option(names = { "--sslon" }, defaultValue = "false", description = "Use SSL.")
    private boolean sslOn;

    @CommandLine.Option(names = { "--sslport" }, defaultValue = "465", description = "SSL port (default is 465).")
    private int sslPort;

    @CommandLine.Option(names = { "-f", "--from" }, required = true, description = "FROM field.")
    private String fromAddress;

    @CommandLine.Option(names = { "-n", "--sender" }, description = "Sender full name (optional).")
    private String senderName;

    @CommandLine.Option(names = { "-s", "--subject" }, required = true, description = "Subject.")
    private String subject;

    @CommandLine.Option(names = { "-b", "--body" }, required = true, description = "Message body (can be HTML).")
    private String message;

    @CommandLine.Option(names = { "--to" }, required = true, description = "List of mandatory TO recipients.",
        split = ",", arity = "1..*")
    private List<String> toList;

    @CommandLine.Option(names = { "--cc" }, description = "List of optional CC recipients.",
        split = ",", arity = "1..*")
    private List<String> ccList;

    @CommandLine.Option(names = { "--bcc" }, description = "List of optional BCC recipients.",
        split = ",", arity = "1..*")
    private List<String> bccList;

    @CommandLine.Option(names = { "--help" }, usageHelp = true, description = "display this help message.")
    private boolean help;

    @CommandLine.Option(names = { "-v", "--version" }, versionHelp = true, description = "print version information and exit.")
    private boolean versionRequested;

    /**
     * Implements the Callable<Integer> interface's call() method
     */
    @Override
    public Integer call() {
        SMTPConfig smtpConfig = new SMTPConfig(
                smtpHost,
                smtpPort,
                user,
                password,
                sslOn,
                sslPort);
        EmailModel emailModel = new EmailModel(
                fromAddress,
                senderName,
                subject,
                message);

        emailModel.setMultipleTo(toList);

        if (ccList != null && !ccList.isEmpty()) {
            emailModel.setMultipleCc(ccList);
        }

        if (bccList != null && !bccList.isEmpty()) {
            emailModel.setMultipleBcc(bccList);
        }

        EmailService emailService = new EmailService(smtpConfig, 1);
        Future<List<String>> errors = emailService.send(emailModel);

        int callResult = 0;
        try {
            List<String> listOfErrors = errors.get();
            if (!listOfErrors.isEmpty()) {
                LOGGER.severe("Errors sending emails: " + listOfErrors.toString());
                callResult = 1;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            callResult = 1;
        }
        emailService.shutdown();

        return callResult;
    }

    /**
     * Executes the CommandLine
     * 
     * @param args cli parameters, to be parsed by picocli
     */
    public static void main(String... args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
