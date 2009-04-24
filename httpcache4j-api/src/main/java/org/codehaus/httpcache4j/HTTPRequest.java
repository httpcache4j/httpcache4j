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

import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.preference.Preferences;

import java.net.URI;
import java.util.*;

/**
 * Represents a HTTP request. You can use this in a couple of ways: <br/>
 * either manipulating the headers directly, or by using the convenience objects.
 * If you manipulate the headers, and use the convenience objects afterwards, the
 * headers produced by the convenience objects takes precedence.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class HTTPRequest {
    private final URI requestURI;
    private final List<Parameter> parameters;
    private final HTTPMethod method;
    private final Conditionals conditionals;
    private final Preferences preferences;
    private Headers headers;
    private Challenge challenge;
    private Payload payload;

    public HTTPRequest(URI requestURI, HTTPMethod method) {
        this.method = method;
        this.requestURI = requestURI;
        this.headers = new Headers();
        this.parameters = new ArrayList<Parameter>();
        this.conditionals = new Conditionals();
        this.preferences = new Preferences();
        String query = requestURI.getQuery();
        if (query != null) {
            parseQuery(query);
        }
    }
    
    public HTTPRequest(URI requestURI) {
        this(requestURI, HTTPMethod.GET);
    }

    private void parseQuery(String query) {
        String[] parts = query.split("&");
        if (parts.length > 0) {
            for (String part : parts) {
                int equalsIndex = part.indexOf('=');
                if (equalsIndex != -1) {
                    Parameter param = new Parameter(part.substring(0, equalsIndex), part.substring(equalsIndex + 1));
                    addParameter(param);
                }
            }
        }
    }

    public URI getRequestURI() {
        return requestURI;
    }

    public Headers getHeaders() {
        return headers;
    }

    /**
     * Returns all headers with the headers from the Conditionals, Payload and Preferences.
     * If you have explicitly set headers on the request that are the same as the Conditionals and Preferences they are overwritten.
     * @return All the headers
     */
    public Headers getAllHeaders() {
        Headers requestHeaders = getHeaders();
        Headers conditionalHeaders = getConditionals().toHeaders();
        Headers preferencesHeaders = getPreferences().toHeaders();

        requestHeaders = merge(merge(requestHeaders, conditionalHeaders), preferencesHeaders);
        if (!requestHeaders.hasHeader(HeaderConstants.CONTENT_TYPE) && hasPayload()) {
            requestHeaders.add(HeaderConstants.CONTENT_TYPE, getPayload().getMimeType().toString());
        }

        //We don't want to add headers more than once.
        return requestHeaders;
    }

    private Headers merge(final Headers base, final Headers toMerge) {
        Map<String, List<Header>> map = new HashMap<String, List<Header>>(base.getHeadersAsMap());
        map.putAll(toMerge.getHeadersAsMap());
        if (map.isEmpty()) {
            return new Headers();
        }
        return new Headers(map);
    }
    


    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public void addHeader(Header header) {
        Validate.notNull(header, "You may not add a null header");
        headers.add(header);
    }

    public void addHeader(String name, String value) {
        Validate.notEmpty(name, "You may not add a null header");
        Validate.notNull(value, "You may not add a null header");
        headers.add(new Header(name, value));
    }

    public void addParameter(Parameter parameter) {
        if (!parameters.contains(parameter)) {
            parameters.add(parameter);
        }
    }

    public void addParameter(String name, String value) {
        addParameter(new Parameter(name, value));
    }

    public Conditionals getConditionals() {
        return conditionals;
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public Preferences getPreferences() {
        return preferences;
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
        this.payload = payload;
    }

    public void setHeaders(final Headers headers) {
        Validate.notNull(headers, "You may not set null headers");
        this.headers = headers;
    }

    public boolean hasPayload() {
        return payload != null;
    }
}