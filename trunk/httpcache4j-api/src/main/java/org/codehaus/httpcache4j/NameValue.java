package org.codehaus.httpcache4j;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * Represents a name/value pair.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public abstract class NameValue implements Serializable {
    protected String name;
    protected String value;
    private static final long serialVersionUID = 8126643664459915558L;

    public NameValue(String value, String name) {
        Validate.notEmpty(name, "You may not have an empty name in a name value combination");
        if (StringUtils.isBlank(value)) {
            value = "";
        }

        this.value = value;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Parameter header = (Parameter) o;

        if (name != null ? !name.equals(header.name) : header.name != null) {
            return false;
        }
        if (value != null ? !value.equals(header.value) : header.value != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "name: " + name + " value: " + value;
    }
}
