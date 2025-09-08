package com.softinstigate.ermes.mail;

import org.apache.commons.mail.HtmlEmail;

public class DefaultHtmlEmailFactory implements HtmlEmailFactory {

    @Override
    public HtmlEmail create() {
        return new HtmlEmail();
    }

}
