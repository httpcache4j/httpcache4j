package org.codehaus.httpcache4j;


import java.util.Locale;
import java.util.Objects;

/**
 * Represents a name/value pair.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public abstract class NameValue {
    protected final String name;
    protected final String value;

    protected NameValue(final String name, String value) {
        Objects.requireNonNull(name != null, "You may not have an null name in a name value combination");
        if (value == null || value.trim().isEmpty()) {
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

        NameValue header = (NameValue) o;

        if (name != null ? !name.equalsIgnoreCase(header.name) : header.name != null) {
            return false;
        }
        if (value != null ? !value.equals(header.value) : header.value != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (name != null ? name.toLowerCase(Locale.ENGLISH).hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "name: " + name + " value: " + value;
    }
}
