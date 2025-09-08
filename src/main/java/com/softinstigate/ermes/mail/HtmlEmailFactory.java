package com.softinstigate.ermes.mail;

import org.apache.commons.mail.HtmlEmail;

/**
 * Factory used to create HtmlEmail instances. Allows tests to inject mocks.
 */
public interface HtmlEmailFactory {
    HtmlEmail create();
}
