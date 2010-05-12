package org.codehaus.httpcache4j;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class QuotedParameter extends Parameter {
    public QuotedParameter(String name, String value) {
        super(name, value);
    }

    public String getQuotedValue() {
        return "\"" + getValue() + "\"";
    }

    @Override
    public String toString() {
        return getName() + "=" + getQuotedValue();
    }
}
