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
