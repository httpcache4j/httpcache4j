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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.codehaus.httpcache4j.Parameter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Immutable URI builder.
 * Paths in this URI builder will be UTF-8 {@link org.codehaus.httpcache4j.util.URIEncoder URIEncoded}.
 * Query Parameters needs to be URIEncoded before they are added.
 * All methods return a NEW instance of the URI builder, meaning you can create a ROOT uri builder and use it
 * to your heart's content, as the instance will never change.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public final class URIBuilder {
    private final String scheme;
    private final String host;
    private final int port;
    private final List<Path> path;
    private final String fragment;
    private final Map<String, List<String>> parameters;

    private URIBuilder(String scheme, String host, int port, List<Path> path, String fragment, Map<String, List<String>> parameters) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.path = path;
        this.fragment = fragment;
        this.parameters = parameters;
    }

    /**
     * This is the scheme to use. Usually 'http' or 'https'.
     * @param scheme the scheme
     * @return a new URIBuilder with the new scheme set.
     */
    public URIBuilder scheme(String scheme) {        
        return new URIBuilder(scheme, host, port, path, fragment, parameters);
    }

    /**
     * this adds the path to a uri.
     * We do not expect the path separator '/' to appear here, as each element will be URLEncoded.
     * If the '/' character do appear it will be URLEncoded with the rest of the path.
     *
     * @param path path elements.
     * @return a new URI builder which contains the new path.
     */
    public URIBuilder path(String... path) {
        List<String> pathList = Arrays.asList(path);
        List<Path> paths = Lists.transform(pathList, stringToPath);
        return new URIBuilder(scheme, host, port, ImmutableList.copyOf(paths), fragment, parameters);
    }

    public URIBuilder host(String host) {
        return new URIBuilder(scheme, host, port, path, fragment, parameters);
    }

    /**
     * Sets the port. This is not required to set if you are using default ports for 'http' or 'https'
     * @param port the port to set
     * @return a new URIBuilder with the port set
     */
    public URIBuilder port(int port) {
        if ("http".equals(scheme) && port == 80) {
            port = -1;
        }
        else if ("https".equals(scheme) && port == 443) {
            port = -1;
        }
        return new URIBuilder(scheme, host, port, path, fragment, parameters);
    }

    public URIBuilder fragment(String fragment) {
        return new URIBuilder(scheme, host, port, path, fragment, parameters);
    }

    /**
     * Creates a new URIBuilder with no parameters, but all other values retained.
     * @return new URIBuilder with no parameters.
     */
    public URIBuilder noParameters() {
        return parameters(Collections.<Parameter>emptyList());
    }

    /**
     * Sets a list of parameters. This will clear out all previously set parameters in the new instance.
     * @param parameters the list of parameters
     * @return new URIBuilder with parameters.
     */
    public URIBuilder parameters(List<Parameter> parameters) {
        Map<String, List<String>> paraMap = new LinkedHashMap<String, List<String>>();
        for (Parameter parameter : parameters) {
            addToQueryMap(paraMap, parameter.getName(), parameter.getValue());
        }
        return new URIBuilder(scheme, host, port, path, fragment, Collections.unmodifiableMap(paraMap));
    }

    /**
     * Adds a new Parameter to the collection of parameters
     * @param name the parameter name
     * @param value the parameter value
     * @return a new instance of the URIBuilder
     */
    public URIBuilder addParameter(String name, String value) {
        return addParameter(new Parameter(name, value));
    }

    /**
     * Adds a new Parameter to the collection of parameters
     * @param parameter the parameter
     * @return a new instance of the URIBuilder
     */
    public URIBuilder addParameter(Parameter parameter) {
        Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>(this.parameters);
        addToQueryMap(parameters, parameter.getName(), parameter.getValue());
        return new URIBuilder(scheme, host, port, path, fragment, Collections.unmodifiableMap(parameters));
    }

    private String toPath() {
        if (path.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (Path pathElement : path) {
            if (builder.length() > 0) {
                builder.append("/");
            }
            builder.append(pathElement.getEncodedString());
        }
        if (host != null && builder.length() > 1) {
            if (!"/".equals(builder.substring(0, 1))) {
                builder.insert(0, "/");                
            }
        }
        return builder.toString();
    }

    public URI toURI() {
        try {
            return new URI(scheme, null, host, port, toPath(), toQuery(), fragment);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @return true if the scheme and host parts are not set.
     */
    public boolean isRelative() {
        return (scheme == null && host == null);
    }

    public URI toAbsoluteURI() {
        if (isRelative()) {
            try {
                String path = toPath();
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                return new URI(null, null, null, -1, path, toQuery(), fragment);
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e);
            }
        }
        return toURI();
    }

    private String toQuery() {
        StringBuilder builder = new StringBuilder();
        for (Parameter parameter : getParametersAsList()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(parameter);
        }
        if (builder.length() == 0) {
            return null;
        }
        return builder.toString();
    }

    private List<Parameter> getParametersAsList() {
        List<Parameter> list = new ArrayList<Parameter>();
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            for (String value : entry.getValue()) {
                list.add(new Parameter(entry.getKey(), value));
            }
        }
        return list;
    }

    /**
     * Constructs a new URIBuilder from the given URI
     * @param uri the uri to use
     * @return a new URIBuilder which has the information from the URI.
     */
    public static URIBuilder fromURI(URI uri) {
        return new URIBuilder(uri.getScheme(), uri.getHost(), uri.getPort(), toPathParts(uri.getPath()), uri.getFragment(), toQueryMap(uri.getQuery()));
    }

    /**
     * Creates an empty URIBuilder.
     * @return an empty URIBuilder which result of {@link #toURI()} ()} will return "".
     */
    public static URIBuilder empty() {
        return new URIBuilder(null, null, -1, Collections.<Path>emptyList(), null, Collections.<String, List<String>>emptyMap());
    }

    private static Map<String, List<String>> toQueryMap(String query) {
        Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
        if (query != null) {
            String[] parts = query.split("&");
            for (String part : parts) {
                String[] equalParts = part.split("=");
                String name = null;
                String value = null;
                if (equalParts.length == 1) {
                    name = equalParts[0];
                }
                else if (equalParts.length == 2) {
                    name = equalParts[0];
                    value = equalParts[1];
                }
                if (name != null) {
                    addToQueryMap(map, URIDecoder.decodeUTF8(name), URIDecoder.decodeUTF8(value));
                }
            }
        }

        return Collections.unmodifiableMap(map);
    }

    private static void addToQueryMap(Map<String, List<String>> map, String name, String value) {
        List<String> list = map.get(name);
        if (list == null) {
            list = new ArrayList<String>();
        }
        list.add(value);
        map.put(name, list);
    }

    private static List<Path> toPathParts(String path) {
        if (path == null) {
            return Collections.emptyList();
        }
        if (!path.contains("/")) {
            return Collections.singletonList(new Path(path));
        }
        else {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            List<String> stringList = Arrays.asList(path.split("/"));
            return ImmutableList.copyOf(Lists.transform(stringList, stringToPath));
        }
    }

    private static Function<String, Path> stringToPath = new Function<String, Path>() {
        public Path apply(String from) {
            return new Path(from);
        }
    };

    private static class Path {
        private final String value;

        private Path(String value) {
            this.value = URIDecoder.decodeUTF8(value);
        }

        String getEncodedString() {
            return URIEncoder.encodeUTF8(value);
        }

        String getValue() {
            return value;
        }
    }
}
