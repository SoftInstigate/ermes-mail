package com.softinstigate.ermes.mail;

import java.util.Set;

/**
 * @author omar
 */
public class EmailConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 8569152854221211947L;

    private static String getMessage(Set<String> missingProperties) {
        var message = "Missing environment configuration parameters: ";
        var res = new String[1];
        res[0] = "";

        missingProperties.forEach(prop -> {
            res[0] += prop + ", ";
        });

        var concatRes = res[0];
        message += concatRes.substring(0, concatRes.length() - 2);

        return message;
    }

    public EmailConfigurationException(Set<String> missingProperties) {
        super(getMessage(missingProperties));
    }

}
