/*
 * Copyright (c) 2009. The Codehaus. All Rights Reserved.
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

package org.codehaus.httpcache4j.util;

import org.codehaus.httpcache4j.Parameter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class URIBuilder {
    private String scheme;
    private String host;
    private int port;
    private String path;
    private String fragment;
    private Map<String, List<Parameter>> parameters = new HashMap<String, List<Parameter>>();

    private URIBuilder(String scheme, String host, int port, String path, String fragment) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.path = path;
        this.fragment = fragment;
    }       

    public URIBuilder path(String path) {
        this.path = path;
        return this;
    }

    public URIBuilder host(String host) {
        this.host = host;
        return this;
    }

    public URIBuilder port(int port) {
        this.port = port;
        return this;
    }

    public URIBuilder fragment(String fragment) {
        this.fragment = fragment;
        return this;
    }

    public URIBuilder addParameter(String name, String value) {
        return addParameter(new Parameter(name, value));
    }

    public URIBuilder addParameter(Parameter parameter) {
        List<Parameter> list = parameters.get(parameter.getName());
        if (list == null) {
            list = new ArrayList<Parameter>();
        }
        list.add(parameter);
        return this;
    }

    public URIBuilder clearParameters() {
        parameters.clear();
        return this;
    }

    public URIBuilder copy() {
        URIBuilder builder = new URIBuilder(scheme, host, port, path, fragment);
        builder.parameters.putAll(parameters);
        return builder;
    }

    public URI toURI() {
        try {
            return new URI(scheme, null, host, port, path, toQuery(), fragment);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private String toQuery() {
        StringBuilder builder = new StringBuilder();
        for (Parameter parameter : getParametersAsList()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(parameter.getName()).append("=").append(parameter.getValue());
        }
        return builder.toString();
    }

    private List<Parameter> getParametersAsList() {
        List<Parameter> list = new ArrayList<Parameter>();
        for (List<Parameter> pList : parameters.values()) {
            list.addAll(pList);
        }
        return list;
    }

    public static URIBuilder fromURI(URI uri) {
        return new URIBuilder(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getFragment());
    }
}
