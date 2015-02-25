package org.codehaus.httpcache4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class AuthDirective extends Directive {
    public AuthDirective(final String name, String value) {
        super(name, value);
    }

    public AuthDirective(final String name, String value, List<Parameter> parameters) {
        super(name, value, parameters);
    }

    @Override
    public String toString() {
        String output = name;
        if (!value.isEmpty()) {
            output += " " + value;
        }
        if (!getParameters().isEmpty()) {
            output = output + " " + getParameters().stream().map(Parameter::toString).collect(Collectors.joining(", "));
        }
        return output;
    }
}
