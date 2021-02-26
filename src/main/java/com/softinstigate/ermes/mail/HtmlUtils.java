/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softinstigate.ermes.mail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author omar
 */
public class HtmlUtils {


    public static String parseTemplate(String filePath, Map<String, String> vars) {
        StringBuilder contentBuilder = new StringBuilder();
        String template;
        try {
            var in = new BufferedReader(new FileReader(filePath));

            while ((template = in.readLine()) != null) {
                contentBuilder.append(template);
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Error while parsing html template: " + e.getMessage());
            
            return null;
        }

        var _result = new String[1];
        _result[0] = contentBuilder.toString();
;

        vars.keySet().forEach(key -> {
            var searchVal = "\\$\\{" + key + "\\}";
            
            _result[0] = _result[0].replaceAll(searchVal, vars.get(key));
            
        });
        
        return _result[0];
    }

}
