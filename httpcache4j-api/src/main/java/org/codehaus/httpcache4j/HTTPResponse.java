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

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public final class HTTPResponse implements Serializable {
    private final Status status;
    private final Payload payload;
    private final Headers headers;
    private Tag ETag;
    private DateTime lastModified;
    private Set<HTTPMethod> allowedMethods;

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
            lastModified = HTTPUtils.fromHttpDate(headers.getFirstHeader(LAST_MODIFIED));
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
}