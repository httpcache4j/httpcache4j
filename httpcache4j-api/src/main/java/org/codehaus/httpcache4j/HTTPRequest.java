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
    private final Headers headers;
    private final Challenge challenge;
    private final Payload payload;

    public HTTPRequest(URI requestURI, HTTPMethod method) {
        this(requestURI, method, Collections.<Parameter>emptyList(), new Headers(), new Conditionals(), new Preferences(), null, null);
    }

    public HTTPRequest(HTTPRequest request) {
        this(request.getRequestURI(),
             request.getMethod(),
             request.getParameters(),
             request.getHeaders(),
             request.getConditionals(),
             request.getPreferences(),
             request.getChallenge(),
             request.getPayload()
        );
    }

    private HTTPRequest(URI requestURI,
                       HTTPMethod method,
                       List<Parameter> parameters,
                       Headers headers,
                       Conditionals conditionals,
                       Preferences preferences,
                       Challenge challenge,
                       Payload payload) {

        this.method = method;
        this.requestURI = requestURI;
        this.headers = headers;
        this.conditionals = conditionals;
        this.preferences = preferences;
        this.challenge = challenge;
        this.payload = payload;
        String query = requestURI.getQuery();
        if (query != null) {
            List<Parameter> list = new ArrayList<Parameter>(parameters);
            list.addAll(parseQuery(query));
            this.parameters = list;
        }
        else {
            this.parameters = parameters;
        }
    }

    public HTTPRequest(URI requestURI) {
        this(requestURI, HTTPMethod.GET);
    }

    private List<Parameter> parseQuery(String query) {
        List<Parameter> parameters = new ArrayList<Parameter>();
        String[] parts = query.split("&");
        if (parts.length > 0) {
            for (String part : parts) {
                int equalsIndex = part.indexOf('=');
                if (equalsIndex != -1) {
                    Parameter param = new Parameter(part.substring(0, equalsIndex), part.substring(equalsIndex + 1));
                    parameters.add(param);
                }
            }
        }
        return parameters;
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
     *
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

    public HTTPRequest addParameter(Parameter parameter) {
        List<Parameter> parameters = new ArrayList<Parameter>(this.parameters);
        if (!parameters.contains(parameter)) {
            parameters.add(parameter);
        }
        return new HTTPRequest(requestURI, method, parameters, headers, conditionals, preferences, challenge, payload);
    }

    public HTTPRequest addParameter(String name, String value) {
        return addParameter(new Parameter(name, value));
    }

    public HTTPRequest addHeader(Header header) {
        Headers headers = new Headers(this.headers);
        headers.add(header);
        return new HTTPRequest(requestURI, method, parameters, headers, conditionals, preferences, challenge, payload);
    }

    public HTTPRequest addHeader(String name, String value) {
        return addHeader(new Header(name, value));
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

    public HTTPRequest challenge(Challenge challenge) {
        return new HTTPRequest(requestURI, method, parameters, headers, conditionals, preferences, challenge, payload);
    }

    public Payload getPayload() {
        return payload;
    }

    public HTTPRequest payload(Payload payload) {
        return new HTTPRequest(requestURI, method, parameters, headers, conditionals, preferences, challenge, payload);
    }

    public HTTPRequest headers(final Headers headers) {
        Validate.notNull(headers, "You may not set null headers");
        return new HTTPRequest(requestURI, method, parameters, headers, conditionals, preferences, challenge, payload);
    }

    public boolean hasPayload() {
        return payload != null;
    }
}