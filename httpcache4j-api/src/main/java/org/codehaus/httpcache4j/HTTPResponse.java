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

import org.apache.commons.lang.Validate;

import static org.codehaus.httpcache4j.HeaderConstants.*;
import org.codehaus.httpcache4j.payload.Payload;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a HTTP response delivered by the cache.
 * Constructions of this should not be done by clients, they should
 * rely on that the cache does its job.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class HTTPResponse implements Serializable {
    private static final long serialVersionUID = -7448511905298678448L;
    
    private final Status status;
    private final Payload payload;
    private final Headers headers;
    private Tag ETag;
    private DateTime lastModified;
    private Set<HTTPMethod> allowedMethods;

    /**
     * Constructs an empty http response with the given status.
     * @param status the status to use.
     */
    HTTPResponse(Status status) {
        this(null, status, new Headers());
    }

    public HTTPResponse(Payload payload, Status status, Headers headers) {
        Validate.notNull(status, "You must supply a Status");
        Validate.notNull(headers, "You must supply some Headers");
        this.status = status;
        this.payload = payload;
        this.headers = headers;

        if (headers.hasHeader(ETAG)) {
            ETag = Tag.parse(headers.getFirstHeader(ETAG).getValue());
        }
        if (headers.hasHeader(LAST_MODIFIED)) {
            lastModified = HeaderUtils.fromHttpDate(headers.getFirstHeader(LAST_MODIFIED));
        }
        if (headers.hasHeader(ALLOW)) {
            String value = headers.getFirstHeader(ALLOW).getValue();
            String[] parts = value.split(",");
            Set<HTTPMethod> allowedMethods = new HashSet<HTTPMethod>();
            for (String part : parts) {
                allowedMethods.add(HTTPMethod.valueOf(part.trim()));
            }
            this.allowedMethods = Collections.unmodifiableSet(allowedMethods);
        }
    }

    public boolean hasPayload() {
        return payload != null;
    }

    public Status getStatus() {
        return status;
    }

    public Payload getPayload() {
        return payload;
    }

    public Headers getHeaders() {
        return headers;
    }

    public Tag getETag() {
        return ETag;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public Set<HTTPMethod> getAllowedMethods() {
        return allowedMethods != null ? allowedMethods : Collections.<HTTPMethod>emptySet();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HTTPResponse response = (HTTPResponse) o;

        if (ETag != null ? !ETag.equals(response.ETag) : response.ETag != null) {
            return false;
        }
        if (allowedMethods != null ? !allowedMethods.equals(response.allowedMethods) : response.allowedMethods != null) {
            return false;
        }
        if (headers != null ? !headers.equals(response.headers) : response.headers != null) {
            return false;
        }
        if (lastModified != null ? !lastModified.equals(response.lastModified) : response.lastModified != null) {
            return false;
        }
        if (payload != null ? !payload.equals(response.payload) : response.payload != null) {
            return false;
        }
        if (status != null ? !status.equals(response.status) : response.status != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (ETag != null ? ETag.hashCode() : 0);
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
        result = 31 * result + (allowedMethods != null ? allowedMethods.hashCode() : 0);
        return result;
    }
}