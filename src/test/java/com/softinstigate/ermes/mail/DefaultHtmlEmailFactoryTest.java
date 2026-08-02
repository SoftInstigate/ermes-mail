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

import org.apache.commons.mail.HtmlEmail;
import org.junit.jupiter.api.Test;

class DefaultHtmlEmailFactoryTest {

    @Test
    void createReturnsNonNull() {
        DefaultHtmlEmailFactory factory = new DefaultHtmlEmailFactory();
        assertNotNull(factory.create());
    }

    @Test
    void createReturnsHtmlEmailInstance() {
        DefaultHtmlEmailFactory factory = new DefaultHtmlEmailFactory();
        assertInstanceOf(HtmlEmail.class, factory.create());
    }

    @Test
    void multipleCallsReturnDistinctInstances() {
        DefaultHtmlEmailFactory factory = new DefaultHtmlEmailFactory();
        HtmlEmail first = factory.create();
        HtmlEmail second = factory.create();
        assertNotSame(first, second);
    }
}
