package org.codehaus.httpcache4j.auth;

import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Headers;

/**
 * @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
 */
public final class NullAuthenticator implements Authenticator, ProxyAuthenticator {
    public final static NullAuthenticator INSTANCE = new NullAuthenticator();

    private NullAuthenticator() {}



    @Override
    public HTTPRequest prepareAuthentication(HTTPRequest request, HTTPResponse response) {
        return request;
    }

    @Override
    public HTTPRequest preparePreemptiveAuthentication(HTTPRequest request) {
        return request;
    }

    @Override
    public void afterSuccessfulAuthentication(HTTPRequest request, Headers responseHeaders) {
    }

    @Override
    public boolean canAuthenticatePreemptively(HTTPRequest request) {
        return false;
    }

    @Override
    public void afterFailedAuthentication(HTTPRequest request, Headers responseHeaders) {
    }

    @Override
    public void invalidateAuthentication() {
    }

    @Override
    public ProxyConfiguration getConfiguration() {
        return null;
    }

    @Override
    public boolean canAuthenticatePreemptively() {
        return false;
    }

    @Override
    public void afterSuccessfulAuthentication(Headers responseHeaders) {
    }

    @Override
    public void afterFailedAuthentication(Headers responseHeaders) {
    }
}
