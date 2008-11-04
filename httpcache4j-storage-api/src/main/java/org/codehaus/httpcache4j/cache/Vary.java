package org.codehaus.httpcache4j.cache;

import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.Header;

import java.io.Serializable;
import java.util.*;


/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public final class Vary implements Serializable {
    private final Map<String, String> varyHeaders = new HashMap<String, String>();

    /**
     * Constructor
     *
     * @param headers the vary headers as keys from the response, with request headers as values.
     */
    public Vary(final Map<String, String> headers) {
        Validate.notNull(headers);
        varyHeaders.putAll(headers);
    }

    /**
     * @return the header names.
     */
    public List<String> getVaryHeaderNames() {
        return Collections.unmodifiableList(new ArrayList<String>(varyHeaders.keySet()));
    }

    /**
     * Analyses the headers in the given request to figure out if this Variation matches.
     *
     * @param request the request to analyse
     * @return {@code true} if the request matches the variance. {@code false} if not.
     */
    public boolean matches(final HTTPRequest request) {
        for (Map.Entry<String, String> varyEntry : varyHeaders.entrySet()) {
            List<Header> requestHeaderValue = request.getHeaders().getHeaders(varyEntry.getKey());
            boolean valid = requestHeaderValue == null ? varyEntry.getValue() == null : request.getHeaders().getFirstHeader(varyEntry.getKey()).getValue().equals(varyEntry.getValue());
            if (!valid) {
                return false;
            }

        }
        return true;
    }

    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Vary vary = (Vary) o;

        if (varyHeaders != null ? !varyHeaders.equals(vary.varyHeaders) : vary.varyHeaders != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return (varyHeaders != null ? varyHeaders.hashCode() : 0);
    }
}