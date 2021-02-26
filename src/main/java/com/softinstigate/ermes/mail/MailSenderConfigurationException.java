/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softinstigate.ermes.mail;

import java.util.Set;

/**
 *
 * @author omar
 */
public class MailSenderConfigurationException extends Exception {

    private static String getMessage(Set<String> missingProperties) {
        var message = "Missing enviroment configuration paramenters: ";
        var res = new String[1];
        res[0] = "";

        missingProperties.forEach(prop -> {
            res[0] += prop + ", ";
        });

        var concatRes = res[0];
        message += concatRes.substring(0, concatRes.length() - 2);
        
        return message;
    }
    
    

    public MailSenderConfigurationException(Set<String> missingProperties) {
        super(getMessage(missingProperties));

    }

}
