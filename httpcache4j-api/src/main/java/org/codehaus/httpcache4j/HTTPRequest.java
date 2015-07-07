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

import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.uri.URIBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a HTTP request. You can use this in a couple of ways:
 *
 * Manipulating the headers directly, or by using the convenience objects.
 *
 * If you manipulate the headers, and use the convenience objects afterwards, the
 * headers produced by the convenience objects takes precedence.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class HTTPRequest {
    private final URI requestURI;
    private final HTTPMethod method;
    private final Headers headers;
    private final Optional<Challenge> challenge;
    private final Optional<Payload> payload;
    private final URI normalizedURI;

    public HTTPRequest(URI requestURI,
                       HTTPMethod method,
                       Headers headers,
                       Optional<Challenge> challenge,
                       Optional<Payload> payload) {

        this.requestURI = Objects.requireNonNull(requestURI, "You MUST have a URI");
        this.normalizedURI = URIBuilder.fromURI(requestURI).toNormalizedURI();
        this.method = method == null ? HTTPMethod.GET : method;
        this.headers = headers == null ? new Headers() : headers;
        this.challenge = Objects.requireNonNull(challenge, "Challenge may not be null");
        this.payload = Objects.requireNonNull(payload, "Payload may not be null");
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
        this(requestURI, method, new Headers(), Optional.<Challenge>empty(), Optional.<Payload>empty());
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
        requestHeaders = hasPayload() ? requestHeaders.withContentType(getPayload().get().getMimeType()) : requestHeaders;
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

    public HTTPRequest setHeader(String name, String value) {
        return headers(headers.set(name, value));
    }

    public HTTPMethod getMethod() {
        return method;
    }

    /**
     * @deprecated Use {@link #withMethod(HTTPMethod)} instead
     */
    @Deprecated
    public HTTPRequest method(HTTPMethod method) {
        return withMethod(method);
    }

    public HTTPRequest withMethod(HTTPMethod method) {
        Objects.requireNonNull(method, "You may not set null method");
        if (method == this.method) return this;
        return new HTTPRequest(requestURI, method, headers, challenge, payload);
    }

    public Optional<Challenge> getChallenge() {
        return challenge;
    }

    /**
     * @deprecated Use {@link #withChallenge(Challenge)} instead
     */
    @Deprecated
    public HTTPRequest challenge(Challenge challenge) {
        return withChallenge(challenge);
    }

    public HTTPRequest withChallenge(Challenge challenge) {
        return new HTTPRequest(requestURI, method, headers, Optional.ofNullable(challenge), payload);
    }

    public Optional<Payload> getPayload() {
        return payload;
    }

    /**
     * @deprecated Use {@link #withPayload(Payload)} instead
     */
    @Deprecated
    public HTTPRequest payload(Payload payload) {
        return withPayload(payload);
    }

    public HTTPRequest withPayload(Payload payload) {
        if (!method.canHavePayload()) {
            throw new IllegalStateException(String.format("Unable to add payload to a %s request", method));
        }
        return new HTTPRequest(requestURI, method, headers, challenge, Optional.ofNullable(payload));
    }

    public HTTPRequest headers(final Headers headers) {
        Objects.requireNonNull(headers, "You may not set null headers");
        return new HTTPRequest(requestURI, method, headers, challenge, payload);
    }

    public boolean hasPayload() {
        return payload.isPresent();
    }

    public Optional<CacheControl> getCacheControl() {
        return headers.getCacheControl();
    }

    @Deprecated
    public HTTPRequest cacheControl(CacheControl cc) {
        return withCacheControl(cc);
    }

    public HTTPRequest withCacheControl(CacheControl cc) {
        return addHeader(cc.toHeader());
    }

    public HTTPRequest addIfNoneMatch(Tag tag) {
        return headers(headers.withConditionals(headers.getConditionals().addIfNoneMatch(tag)));
    }

    public HTTPRequest addIfMatch(Tag tag) {
        return headers(headers.withConditionals(headers.getConditionals().addIfMatch(tag)));
    }

    public HTTPRequest withIfUnModifiedSince(LocalDateTime dt) {
        return headers(headers.withConditionals(headers.getConditionals().ifUnModifiedSince(dt)));
    }

    public HTTPRequest withIfModifiedSince(LocalDateTime dt) {
        return headers(headers.withConditionals(headers.getConditionals().ifModifiedSince(dt)));
    }

    public boolean isSecure() {
        return "https".equalsIgnoreCase(requestURI.getScheme());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final HTTPRequest that = (HTTPRequest) o;

        if (challenge != null ? !challenge.equals(that.challenge) : that.challenge != null) {
            return false;
        }
        if (headers != null ? !headers.equals(that.headers) : that.headers != null) {
            return false;
        }
        if (method != null ? !method.equals(that.method) : that.method != null) {
            return false;
        }
        if (normalizedURI != null ? !normalizedURI.equals(that.normalizedURI) : that.normalizedURI != null) {
            return false;
        }
        return !(payload != null ? !payload.equals(that.payload) : that.payload != null);
    }

    @Override
    public int hashCode() {
        int result = method != null ? method.hashCode() : 0;
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (challenge != null ? challenge.hashCode() : 0);
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        result = 31 * result + (normalizedURI != null ? normalizedURI.hashCode() : 0);
        return result;
    }
}
