package org.codehaus.httpcache4j.auth.bearer;


import net.hamnaberg.funclite.Preconditions;
import org.codehaus.httpcache4j.util.StringUtils;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class BearerToken {
    private String token;

    public BearerToken(String token) {
        Preconditions.checkArgument(!StringUtils.isNullOrEmpty(token), "Token was null or empty");
        this.token = token;
    }

    public String getToken() {
        return token;
    }
    
    public String toHeaderValue() {
        return "Bearer " + token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BearerToken that = (BearerToken) o;

        if (token != null ? !token.equals(that.token) : that.token != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return token != null ? token.hashCode() : 0;
    }
}
