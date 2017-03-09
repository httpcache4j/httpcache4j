package org.codehaus.httpcache4j.auth.bearer;

import org.codehaus.httpcache4j.Challenge;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HeaderConstants;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.auth.AuthScheme;
import org.codehaus.httpcache4j.auth.AuthenticatorStrategy;

/**
 * http://datatracker.ietf.org/doc/draft-ietf-oauth-v2-bearer/?include_text=1
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class BearerAuthenticatorStrategy implements AuthenticatorStrategy {
    public boolean supports(AuthScheme scheme) {
        return "Bearer".equalsIgnoreCase(scheme.getType());
    }

    public HTTPRequest prepare(HTTPRequest request, AuthScheme scheme) {
        HTTPRequest req = request;
        Challenge challenge = request.getChallenge().orElse(null);
        if (challenge instanceof BearerTokenChallenge) {
            BearerTokenChallenge token = (BearerTokenChallenge) challenge;
            req = req.addHeader(HeaderConstants.AUTHORIZATION, token.getToken().toHeaderValue());
        }
        return req;
    }

    public HTTPRequest prepareWithProxy(HTTPRequest request, Challenge challenge, AuthScheme scheme) {
        throw new UnsupportedOperationException("Proxy authentication is not handled by the Bearer spec");
    }

    public AuthScheme afterSuccessfulAuthentication(AuthScheme scheme, Headers headers) {
        return scheme;
    }

    public AuthScheme afterSuccessfulProxyAuthentication(AuthScheme scheme, Headers headers) {
        throw new UnsupportedOperationException("Proxy authentication is not handled by the Bearer spec");
    }
}
