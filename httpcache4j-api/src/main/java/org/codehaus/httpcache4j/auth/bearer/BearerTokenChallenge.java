package org.codehaus.httpcache4j.auth.bearer;

import org.codehaus.httpcache4j.Challenge;

import java.util.Objects;

/**
 * <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class BearerTokenChallenge implements Challenge {
    private BearerToken token;

    public BearerTokenChallenge(BearerToken token) {
        this.token = Objects.requireNonNull(token, "Token may not be null");
    }

    public String getIdentifier() {
        return token.getToken();
    }

    public BearerToken getToken() {
        return token;
    }
}
