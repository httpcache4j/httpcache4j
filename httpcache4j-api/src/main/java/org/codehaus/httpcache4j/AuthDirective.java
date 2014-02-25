package org.codehaus.httpcache4j;


import net.hamnaberg.funclite.CollectionOps;

import java.util.List;

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
        if (value != null && !value.isEmpty()) {
            output += " " + value;
        }
        if (!getParameters().isEmpty()) {
            output = output + " " + CollectionOps.mkString(getParameters(), ", ");
        }
        return output;
    }
}
