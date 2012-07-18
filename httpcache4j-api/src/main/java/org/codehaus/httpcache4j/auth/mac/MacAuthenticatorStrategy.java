package org.codehaus.httpcache4j.auth.mac;

import org.codehaus.httpcache4j.Challenge;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HeaderConstants;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.auth.AuthScheme;
import org.codehaus.httpcache4j.auth.AuthenticatorStrategy;
import org.codehaus.httpcache4j.util.Pair;

/**
 * http://tools.ietf.org/html/draft-ietf-oauth-v2-http-mac
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class MacAuthenticatorStrategy implements AuthenticatorStrategy {

    public boolean supports(AuthScheme scheme) {
        return "MAC".equalsIgnoreCase(scheme.getType());
    }

    public HTTPRequest prepare(HTTPRequest request, AuthScheme scheme) {
        String error = scheme.getDirective().getParameterValue("error");
        if (error != null) {
            return request;
        }
        Challenge challenge = request.getChallenge();
        if (challenge instanceof MacChallenge) {
            MacChallenge c = (MacChallenge) challenge;
            Pair<HTTPRequest,String> calculate = c.getExtensionCalculator().calculate(request);
            if (request != calculate.getKey()) {
                request = calculate.getKey();
            }
            RequestMAC requestMAC = new RequestMAC(c.getKey(), Nonce.generate(), calculate.getValue());
            return request.addHeader(HeaderConstants.WWW_AUTHENTICATE, requestMAC.toHeaderValue(request, c.getIdentifier(), c.getAlgorithm()));
        }
        return request;
    }

    public HTTPRequest prepareWithProxy(HTTPRequest request, Challenge challenge, AuthScheme scheme) {
        throw new UnsupportedOperationException("Not Supported by draft. This SHOULD never happen");
    }

    public AuthScheme afterSuccessfulAuthentication(AuthScheme scheme, Headers headers) {
        return scheme;
    }

    public AuthScheme afterSuccessfulProxyAuthentication(AuthScheme scheme, Headers headers) {
        throw new UnsupportedOperationException("Not Supported by draft. This SHOULD never happen");
    }
}
