/*
 * Copyright (c) 2008, The Codehaus. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.codehaus.httpcache4j;


import net.hamnaberg.funclite.Preconditions;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.uri.URIBuilder;
import org.joda.time.DateTime;

import java.net.URI;

/**
 * Represents a HTTP request. You can use this in a couple of ways: <br/>
 * either manipulating the headers directly, or by using the convenience objects.
 * If you manipulate the headers, and use the convenience objects afterwards, the
 * headers produced by the convenience objects takes precedence.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class HTTPRequest {
    private final URI requestURI;
    private final HTTPMethod method;
    private final Headers headers;
    private final Challenge challenge;
    private final Payload payload;
    private URI normalizedURI;

    public HTTPRequest(URI requestURI,
                       HTTPMethod method,
                       Headers headers,
                       Challenge challenge,
                       Payload payload) {

        this.requestURI = Preconditions.checkNotNull(requestURI, "You MUST have a URI");
        this.normalizedURI = URIBuilder.fromURI(requestURI).toNormalizedURI();
        this.method = method == null ? HTTPMethod.GET : method;
        this.headers = headers == null ? new Headers() : headers;
        this.challenge = challenge;
        this.payload = payload;
    }

    public HTTPRequest copy() {
        return new HTTPRequest(
                getRequestURI(),
                getMethod(),
                getHeaders(),
                getChallenge(),
                getPayload()
        );
    }

    public HTTPRequest(URI requestURI) {
        this(requestURI, HTTPMethod.GET);
    }

    public HTTPRequest(String requestURI) {
        this(URI.create(requestURI), HTTPMethod.GET);
    }
    
    public HTTPRequest(String requestURI, HTTPMethod method) {
        this(URI.create(requestURI), method);
    }

    public HTTPRequest(URI requestURI, HTTPMethod method) {
        this(requestURI, method, new Headers(), null, null);
    }

    public URI getRequestURI() {
        return requestURI;
    }

    public URI getNormalizedURI() {
        return normalizedURI;
    }

    public Headers getHeaders() {
        return headers;
    }

    /**
     * Returns all headers with the headers from the Payload
     *
     * @return All the headers
     */
    public Headers getAllHeaders() {
        Headers requestHeaders = getHeaders();
        if (hasPayload()) {
            requestHeaders = requestHeaders.set(HeaderConstants.CONTENT_TYPE, getPayload().getMimeType().toString());
        }

        //We don't want to add headers more than once.
        return requestHeaders;
    }

    public HTTPRequest addHeader(Header header) {
        Headers headers = this.headers.add(header);
        return new HTTPRequest(requestURI, method, headers, challenge, payload);
    }

    public HTTPRequest addHeader(String name, String value) {
        return addHeader(new Header(name, value));
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public HTTPRequest method(HTTPMethod method) {
        Preconditions.checkNotNull(method, "You may not set null method");
        if (method == this.method) return this;
        return new HTTPRequest(requestURI, method, headers, challenge, payload);
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public HTTPRequest challenge(Challenge challenge) {
        return new HTTPRequest(requestURI, method, headers, challenge, payload);
    }

    public Payload getPayload() {
        return payload;
    }

    public HTTPRequest payload(Payload payload) {
        if (!method.canHavePayload()) {
            throw new IllegalStateException(String.format("Unable to add payload to a %s request", method));
        }        
        return new HTTPRequest(requestURI, method, headers, challenge, payload);
    }

    public HTTPRequest headers(final Headers headers) {
        Preconditions.checkNotNull(headers, "You may not set null headers");
        return new HTTPRequest(requestURI, method, headers, challenge, payload);
    }

    public boolean hasPayload() {
        return payload != null;
    }

    public CacheControl getCacheControl() {
        if (headers.contains(HeaderConstants.CACHE_CONTROL)) {
            return new CacheControl(headers.getFirstHeader(HeaderConstants.CACHE_CONTROL));
        }
        return null;
    }

    public HTTPRequest cacheControl(CacheControl cc) {
        return addHeader(cc.toHeader());
    }

    public HTTPRequest addIfNoneMatch(Tag tag) {
        return headers(headers.withConditionals(headers.getConditionals().addIfNoneMatch(tag)));
    }

    public HTTPRequest addIfMatch(Tag tag) {
        return headers(headers.withConditionals(headers.getConditionals().addIfMatch(tag)));
    }

    public HTTPRequest withIfUnModifiedSince(DateTime dt) {
        return headers(headers.withConditionals(headers.getConditionals().ifUnModifiedSince(dt)));
    }

    public HTTPRequest withIfModifiedSince(DateTime dt) {
        return headers(headers.withConditionals(headers.getConditionals().ifModifiedSince(dt)));
    }

    public boolean isSecure() {
        return "https".equalsIgnoreCase(requestURI.getScheme());
    }
}
