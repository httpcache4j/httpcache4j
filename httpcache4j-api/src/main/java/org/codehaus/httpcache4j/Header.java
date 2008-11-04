package org.codehaus.httpcache4j;

import org.apache.commons.lang.Validate;

import java.util.*;

public class Header extends Parameter {
    private Map<String, String> directives = new HashMap<String, String>();

    public Header(String name, String value) {
        super(name, value);
        Validate.notEmpty(name);
        Validate.notEmpty(value);
        parseDirectives(value);
    }


    private void parseDirectives(String value) {
        List<String> directives = Arrays.asList(value.split(","));
        for (String directive : directives) {
            directive = directive.trim();
            if (directive.length() > 0) {
                String[] directiveParts = directive.split("=", 2);
                this.directives.put(directiveParts[0], directiveParts.length > 1 ? directiveParts[1] : null);
            }
        }
    }

    public Map<String, String> getDirectives() {
        return Collections.unmodifiableMap(directives);
    }
}