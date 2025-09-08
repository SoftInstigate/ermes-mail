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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

/**
 * Start MailHog mock SMTP server first: https://github.com/mailhog/MailHog
 */
class EmailServiceIT {

    @Test
    void send() throws ExecutionException, InterruptedException {
        SMTPConfig smtpConfig = new SMTPConfig("localhost", 1025, "user", "password", false);

        EmailModel emailModel = new EmailModel("dick.silly@domain.com", "Dick Silly",
                "Integration Test - " + System.currentTimeMillis(),
                "This is a <strong>HTML</strong> message.");
        emailModel.addTo("john.doe@email.com", "John Doe");
        emailModel.addTo("serena.wiliams@email.com", "Serena Wiliams");
        emailModel.addCc("tom.clancy@email.com", "Tom Clancy");
        emailModel.addBcc("ann.smith@email.com", "Ann Smith");

        EmailService emailService = new EmailService(smtpConfig, 3);
        List<String> errors = emailService.sendSynch(emailModel);

        emailService.shutdown();
        assertTrue(errors.isEmpty());
    }

}
