package com.softinstigate.ermes.mail;

import org.apache.commons.mail.HtmlEmail;

/**
 * Production implementation of {@link HtmlEmailFactory} that creates real
 * {@link HtmlEmail} instances via {@code new HtmlEmail()}.
 */
public class DefaultHtmlEmailFactory implements HtmlEmailFactory {

    @Override
    public HtmlEmail create() {
        return new HtmlEmail();
    }

}
