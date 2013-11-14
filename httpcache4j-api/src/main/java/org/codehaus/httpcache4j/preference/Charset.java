package org.codehaus.httpcache4j.preference;

/**
 * @author Erlend Hamnaberg<hamnis@codehaus.org>
 */
public class Charset {
    private final String name;

    public Charset(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Charset charset = (Charset) o;

        if (name != null ? !name.equals(charset.name) : charset.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return name;
    }
}
