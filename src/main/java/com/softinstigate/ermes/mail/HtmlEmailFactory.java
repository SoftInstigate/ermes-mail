package com.softinstigate.ermes.mail;

import org.apache.commons.mail.HtmlEmail;

/**
 * Factory used to create {@link HtmlEmail} instances.
 * Allows tests to inject mocks instead of real email objects.
 */
public interface HtmlEmailFactory {
    /**
     * Create a new {@link HtmlEmail} instance.
     *
     * @return a new HtmlEmail
     */
    HtmlEmail create();
}
