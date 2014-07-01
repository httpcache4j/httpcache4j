package org.codehaus.httpcache4j.auth.bearer;


import net.hamnaberg.funclite.Preconditions;
import org.codehaus.httpcache4j.Challenge;

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
public final class BearerTokenChallenge implements Challenge {
    private BearerToken token;

    public BearerTokenChallenge(BearerToken token) {
        this.token = Preconditions.checkNotNull(token, "Token may not be null");
    }

    public String getIdentifier() {
        return token.getToken();
    }

    public BearerToken getToken() {
        return token;
    }
}
