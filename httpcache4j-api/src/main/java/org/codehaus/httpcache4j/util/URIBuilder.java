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
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.codehaus.httpcache4j.Parameter;
import org.codehaus.httpcache4j.Parameters;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
    public static AtomicReference<URISchemeDefaults> schemeDefaults = new AtomicReference<URISchemeDefaults>(new URISchemeDefaults());

    private final String scheme;
    private final String host;
    private final int port;
    private final List<Path> path;
    private final String fragment;
    private final Parameters parameters;
    private final boolean wasPathAbsolute;
    private final boolean endsWithSlash;
    private final String schemeSpecificPart;

    private URIBuilder(String scheme, String schemeSpecificPart, String host, int port, List<Path> path, String fragment, Parameters parameters, boolean wasPathAbsolute, boolean endsWithSlash) {
        this.scheme = scheme;
        this.schemeSpecificPart = schemeSpecificPart;
        this.host = host;
        this.port = port;
        this.path = path;
        this.fragment = fragment;
        this.parameters = parameters;
        this.wasPathAbsolute = wasPathAbsolute;
        this.endsWithSlash = endsWithSlash;
    }

    public URIBuilder withHost(String host) {
        return new URIBuilder(scheme, schemeSpecificPart, host, port, path, fragment, parameters, wasPathAbsolute, endsWithSlash);
    }

    /**
     * Sets the port. This is not required to set if you are using default ports for 'http' or 'https'
     * @param port the port to set
     * @return a new URIBuilder with the port set
     */
    public URIBuilder withPort(int port) {
        Optional<Integer> defaultPort = schemeDefaults.get().getPort(scheme);
        if (defaultPort.isPresent() && (port == defaultPort.get())) {
            port = -1;
        }
        return new URIBuilder(scheme, schemeSpecificPart, host, port, path, fragment, parameters, wasPathAbsolute, endsWithSlash);
    }

    private boolean isURN() {
        return scheme != null && scheme.startsWith("urn");
    }


    /**
     * This is the scheme to use. Usually 'http' or 'https'.
     * @param scheme the scheme
     * @return a new URIBuilder with the new scheme set.
     */
    public URIBuilder withScheme(String scheme) {
        return new URIBuilder(scheme, schemeSpecificPart, host, port, path, fragment, parameters, wasPathAbsolute, endsWithSlash);
    }

    /**
     * Adds a raw path to the URI.
     * @param path a path which may contain '/'
     * @return a new URI builder which contains the added path.
     */
    public URIBuilder addRawPath(String path) {
        boolean pathAbsolute = wasPathAbsolute || this.path.isEmpty() && path.startsWith("/");
        boolean endsWithSlash = this.endsWithSlash || this.path.isEmpty() && path.endsWith("/");
        List<Path> appendedPath = toPathParts(path);
        ImmutableList.Builder<Path> currentPath = ImmutableList.builder();
        currentPath.addAll(this.path);
        currentPath.addAll(appendedPath);
        return new URIBuilder(scheme, schemeSpecificPart, host, port, currentPath.build(), fragment, parameters, pathAbsolute, endsWithSlash);

    }

    /**
     * Appends the path part to the URI.
     * We do not expect the path separator '/' to appear here, as each element will be URLEncoded.
     * If the '/' character do appear it will be URLEncoded with the rest of the path.
     *
     * @param path path elements.
     * @return a new URI builder which contains the new path.
     */
    public URIBuilder addPath(List<String> path) {
        List<Path> appendedPath = Lists.transform(path, stringToPath);
        ImmutableList.Builder<Path> currentPath = ImmutableList.builder();
        currentPath.addAll(this.path);
        currentPath.addAll(appendedPath);
        return new URIBuilder(scheme, schemeSpecificPart, host, port, currentPath.build(), fragment, parameters, wasPathAbsolute, endsWithSlash);
    }

    /**
     * @see #addPath(java.util.List)
     *
     * @param path path elements
     * @return a new URI builder which contains the new path.
     */
    public URIBuilder addPath(String... path) {
        return addPath(Arrays.asList(path));
    }

    /**
     * @see #withPath(java.util.List)
     *
     * @param path path elements.
     * @return a new URI builder which contains the new path.
     */
    public URIBuilder withPath(String... path) {
        return withPath(Arrays.asList(path));
    }

    /**
     * Sets the path of the uri.
     * We do not expect the path separator '/' to appear here, as each element will be URLEncoded.
     * If the '/' character do appear it will be URLEncoded with the rest of the path.
     *
     * @param pathList path elements.
     * @return a new URI builder which contains the new path.
     */
    public URIBuilder withPath(List<String> pathList) {
        List<Path> paths = Lists.transform(pathList, stringToPath);
        return pathInternal(paths, false, false);
    }

    /**
     * @see #withPath(java.util.List)
     *
     * @param path path elements.
     * @return a new URI builder which contains the new path.
     */
    public URIBuilder withRawPath(String path) {
        boolean pathAbsoulute = path.startsWith("/");
        boolean endsWithSlash = path.endsWith("/");
        List<Path> parts = toPathParts(path);
        return pathInternal(parts, pathAbsoulute, endsWithSlash);
    }

    private URIBuilder pathInternal(List<Path> pathList, boolean pathAbsolute, boolean endsWithSlash) {
        return new URIBuilder(scheme, schemeSpecificPart, host, port, ImmutableList.copyOf(pathList), fragment, parameters, pathAbsolute, endsWithSlash);
    }

    public URIBuilder withFragment(String fragment) {
        return new URIBuilder(scheme, schemeSpecificPart, host, port, path, fragment, parameters, wasPathAbsolute, endsWithSlash);
    }

    /**
     * Creates a new URIBuilder with no parameters, but all other values retained.
     * @return new URIBuilder with no parameters.
     */
    public URIBuilder noParameters() {
        return withParameters(parameters.empty());
    }

    /**
     * Sets a list of parameters. This will clear out all previously set parameters in the new instance.
     * @param parameters the list of parameters
     * @return new URIBuilder with parameters.
     */
    public URIBuilder withParameters(Iterable<Parameter> parameters) {
        Parameters updated = this.parameters.set(parameters);
        return withParameters(updated);
    }

    public URIBuilder withParameters(Map<String, List<String>> params) {
        Parameters updated = this.parameters.set(params);
        return withParameters(updated);
    }

    public URIBuilder withParameters(Parameters params) {
        return new URIBuilder(scheme, schemeSpecificPart, host, port, path, fragment, params, wasPathAbsolute, endsWithSlash);
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
        return addParameters(Arrays.asList(parameter));
    }

    /**
     * Adds Parameters to the collection of parameters
     * @return a new instance of the URIBuilder
     */
    public URIBuilder addParameters(Iterable<Parameter> newParams) {
        if (!newParams.iterator().hasNext()) {
            return this;
        }
        Parameters updated = this.parameters.add(newParams);
        return withParameters(updated);
    }

    public URIBuilder addParameters(String name, String... values) {
        Parameters updated = this.parameters.add(name, values);
        return withParameters(updated);
    }

    public URIBuilder addParameters(Map<String, List<String>> newParams) {
        if (newParams.isEmpty()) {
            return this;
        }
        Parameters updated = this.parameters.add(newParams);
        return withParameters(updated);
    }

    public URIBuilder removeParameters(String name) {
        return withParameters(this.parameters.remove(name));
    }

    public URIBuilder replaceParameter(String name, String value) {
        return withParameters(this.parameters.set(name, value));
    }

    private String toPath(boolean encodepath) {
        if (path.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (Path pathElement : path) {
            if (builder.length() > 0) {
                builder.append("/");
            }
            builder.append(encodepath ? pathElement.getEncodedValue() : pathElement.getValue());
        }
        if ((wasPathAbsolute || host != null) && builder.length() > 1) {
            if (!"/".equals(builder.substring(0, 1))) {
                builder.insert(0, "/");                
            }
        }
        if (endsWithSlash) {
            builder.append("/");
        }
        return builder.toString();
    }

    public URI toURI() {
        return toURI(true, false, false);
    }

    public URI toNormalizedURI(boolean encodePath) {
        return toURI(encodePath, true, false).normalize();
    }

    public URI toNormalizedURI() {
        return toNormalizedURI(true);
    }

    /**
     * @return true if the scheme and host parts are not set.
     */
    public boolean isRelative() {
        return (scheme == null && host == null);
    }

    public URI toAbsoluteURI() {
        return toURI(true, false, true);
    }

    private URI toURI(boolean encodePath, boolean sortQP, boolean absolutify) {
        try {
            if (isURN()) {
                return new URI(scheme, schemeSpecificPart, fragment);
            }
            StringBuilder sb = new StringBuilder();
            if (scheme != null) {
                sb.append(scheme);
                sb.append("://");
            }
            if (host != null) {
                sb.append(host);
            }
            if (port > -1) {
                sb.append(":").append(port);
            }
            if (!path.isEmpty()) {
                String path = toPath(encodePath);
                if (absolutify && isRelative() && !path.startsWith("/")) {
                    path = "/" + path;
                }
                sb.append(path);
            }
            if (!parameters.isEmpty()) {
                sb.append("?");
                sb.append(parameters.toQuery(sortQP));
            }
            if (fragment != null) {
                sb.append("#");
                sb.append(fragment);
            }
            return URI.create(sb.toString());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    
    public List<Parameter> getParametersByName(final String name) {
        List<String> params = parameters.get(name);
        if (params == null) {
            return Collections.emptyList();
        }
        return Lists.transform(params, new Function<String, Parameter>() {
            public Parameter apply(String s) {
                return new Parameter(name, s);
            }
        });
    }

    public String getFirstParameterValueByName(final String name) {
        return parameters.getFirst(name);
    }

    /**
     * Constructs a new URIBuilder from the given URI
     * @param uri the uri to use
     * @return a new URIBuilder which has the information from the URI.
     */
    public static URIBuilder fromURI(URI uri) {
        boolean pathAbsoluteness = uri.getPath() != null && uri.getPath().startsWith("/");
        boolean endsWithSlash = uri.getPath() != null && uri.getPath().endsWith("/");
        return new URIBuilder(uri.getScheme(), uri.getSchemeSpecificPart(), uri.getHost(), uri.getPort(), toPathParts(uri.getPath()), uri.getFragment(), Parameters.parse(uri.getQuery()), pathAbsoluteness, endsWithSlash);
    }

    /**
     * Creates an empty URIBuilder.
     * @return an empty URIBuilder which result of {@link #toURI()} ()} will return "".
     */
    public static URIBuilder empty() {
        return new URIBuilder(null, "", null, -1, Collections.<Path>emptyList(), null, new Parameters(), false, false);
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public List<String> getPath() {
        return Lists.transform(path, pathToString);
    }

    public List<String> getEncodedPath() {
        return Lists.transform(path, encodedPathToString);
    }

    public String getCurrentPath() {
        return toPath(false);
    }

    public String getFragment() {
        return fragment;
    }

    public Iterable<Parameter> getParameters() {
        return parameters;
    }

    public Parameters getParametersAsObject() {
        return parameters;
    }

    public Map<String, List<String>> getParametersAsMap() {
        return parameters.asMap();
    }

    /**
     * @deprecated use {@link Parameters#parse(String)} instead.
     */
    @Deprecated
    public static Map<String, List<String>> toQueryMap(String query) {
        return Parameters.parse(query).asMap();
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

    private static Function<Path, String> pathToString = new Function<Path, String>() {
        public String apply(Path from) {
            return from.getValue();
        }
    };

    private static Function<Path, String> encodedPathToString = new Function<Path, String>() {
        public String apply(Path from) {
            return from.getEncodedValue();
        }
    };

    private static class Path {
        private final String value;

        private Path(String value) {
            this.value = URIDecoder.decodeUTF8(value);
        }

        String getEncodedValue() {
            return URIEncoder.encodeUTF8(value);
        }

        String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
