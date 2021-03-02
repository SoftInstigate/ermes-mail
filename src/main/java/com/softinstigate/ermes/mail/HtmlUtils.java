package com.softinstigate.ermes.mail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * @author omar
 */
public class HtmlUtils {

    public static String parseTemplate(String filePath, Map<String, String> vars) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        String template;

        try (BufferedReader in = new BufferedReader(new FileReader(filePath))) {
            while ((template = in.readLine()) != null) {
                contentBuilder.append(template);
            }
        }

        var _result = new String[1];
        _result[0] = contentBuilder.toString();

        vars.keySet().forEach(key -> {
            var searchVal = "\\$\\{" + key + "\\}";
            _result[0] = _result[0].replaceAll(searchVal, vars.get(key));
        });

        return _result[0];
    }

}
