/*
 * Copyright (c) 2010. The Codehaus. All Rights Reserved.
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
 */

package org.codehaus.httpcache4j.mutable;

import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.payload.Payload;
import org.joda.time.DateTime;

import java.net.URI;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class MutableRequest {
    private final URI uri;
    private final HTTPMethod method;
    private final MutableHeaders headers;
    private final MutableConditionals conditionals;
    private Challenge challenge;
    private Payload payload;

    public MutableRequest(URI uri) {
        this(uri, HTTPMethod.GET);
    }

    public MutableRequest(URI uri, HTTPMethod method) {
        this(uri, method, new MutableHeaders(), new MutableConditionals());
    }

    private MutableRequest(URI uri,
                           HTTPMethod method,
                           MutableHeaders headers,
                           MutableConditionals conditionals) {
        this.uri = uri;
        this.method = method;
        this.headers = headers;
        this.conditionals = conditionals;
    }

    public URI getRequestURI() {
        return uri;
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public MutableHeaders getHeaders() {
        return headers;
    }

    public MutableConditionals getConditionals() {
        return conditionals;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        if (!method.canHavePayload()) {
            throw new IllegalStateException(String.format("Unable to add payload to a %s request", method));
        }
        this.payload = payload;
    }

    public void addHeader(Header header) {
        headers.add(header);
    }

    public void addHeader(String name, String value) {
        addHeader(new Header(name, value));
    }

    public HTTPRequest toRequest() {
        Headers heads = headers.toHeaders().withConditionals(conditionals.toConditionals());

        return new HTTPRequest(
                uri,
                method,
                heads,
                challenge,
                payload,
                new DateTime()
        );
    }

    public static MutableRequest fromHTTPRequest(HTTPRequest request) {
        MutableRequest mutableRequest = new MutableRequest(
                request.getRequestURI(),
                request.getMethod(),
                new MutableHeaders(request.getHeaders()),
                new MutableConditionals(request.getHeaders().getConditionals())
        );
        mutableRequest.setChallenge(request.getChallenge());
        if (request.getMethod().canHavePayload()) {
            mutableRequest.setPayload(request.getPayload());
        }
        return mutableRequest;
    }

}
