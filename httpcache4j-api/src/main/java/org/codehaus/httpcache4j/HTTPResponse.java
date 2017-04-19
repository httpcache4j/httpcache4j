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

import org.codehaus.httpcache4j.annotation.Internal;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.util.ThrowableFunction;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static org.codehaus.httpcache4j.HeaderConstants.X_CACHE;

/**
 * Represents a HTTP response delivered by the cache.
 * Constructions of this should not be done by clients, they should
 * rely on that the cache does its job.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class HTTPResponse {

    private final StatusLine statusLine;
    private final Optional<Payload> payload;
    private final Headers headers;
    private final boolean cached;

    public HTTPResponse(Status status, Headers headers) {
        this(Optional.<Payload>empty(), new StatusLine(status), headers);
    }

    public HTTPResponse(Optional<Payload> payload, Status status, Headers headers) {
        this(payload, new StatusLine(status), headers);
    }

    public HTTPResponse(Optional<Payload> payload, StatusLine statusLine, Headers headers) {
        this.statusLine = Objects.requireNonNull(statusLine, "You must supply a Status");
        this.payload = Objects.requireNonNull(payload, "We need an optional payload, not null");
        this.headers = Objects.requireNonNull(headers, "You must supply some Headers");

        if (headers.contains(X_CACHE)) {
            Header cacheHeader = CacheHeaderBuilder.getBuilder().createHITXCacheHeader();
            List<Header> xcacheHeaders = headers.getHeaders(X_CACHE);
            cached = xcacheHeaders.contains(cacheHeader);
        } else {
            cached = false;
        }
    }

    @Internal
    public HTTPResponse withHeaders(Headers headers) {
        return new HTTPResponse(payload, statusLine, headers);
    }

    @Internal
    public HTTPResponse withPayload(Payload payload) {
        return new HTTPResponse(Optional.ofNullable(payload), statusLine, headers);
    }

    public boolean hasPayload() {
        return payload.isPresent();
    }

    public Status getStatus() {
        return statusLine.getStatus();
    }

    public StatusLine getStatusLine() {
        return statusLine;
    }

    public Optional<Payload> getPayload() {
        return payload;
    }

    public Headers getHeaders() {
        return headers;
    }

    public boolean isCached() {
        return cached;
    }

    public <A> Optional<A> transform(final ThrowableFunction<Payload, Optional<A>, IOException> f) {
        return payload.flatMap(p -> p.transform(is -> f.apply(new InputStreamPayload(is, p.getMimeType(), p.length()))));
    }

    public void consume() {
        payload.ifPresent(p -> {
            try {
               p.close();
            } catch (IOException ignored){}
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HTTPResponse response = (HTTPResponse) o;

        if (headers != null ? !headers.equals(response.headers) : response.headers != null) {
            return false;
        }
        if (payload != null ? !payload.equals(response.payload) : response.payload != null) {
            return false;
        }
        if (statusLine != response.statusLine) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = statusLine != null ? statusLine.hashCode() : 0;
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        return result;
    }
}
