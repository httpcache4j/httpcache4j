package org.codehaus.httpcache4j;

import java.io.Serializable;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @author last modified by $Author: $
 * @version $Id: $
 */
public class Parameter implements Serializable {
    protected String name;
    protected String value;

    public Parameter(String name, String value) {
        this.name = name;
        this.value = value;
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