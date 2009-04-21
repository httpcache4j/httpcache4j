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

package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.*;
import static org.codehaus.httpcache4j.HeaderConstants.*;
import org.codehaus.httpcache4j.resolver.ResponseResolver;

import org.apache.commons.lang.Validate;

import java.util.*;
import java.io.IOException;
import java.net.SocketException;

/**
 * TODO:
 * Support Warning header http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46
 * Support detecting empty responses? http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.4
 * Support Range headers. http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35
 *
 */


/**
 * The main HTTPCache class.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * */
public class HTTPCache {
    /** Directive to the {@code "Cache-Control"} header */
    public static final String HEADER_DIRECTIVE_MAX_AGE = "max-age";

    private static final Set<HTTPMethod> safeMethods;
    private static final Set<String> unmodifiableHeaders;

    static {
        // ref http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.1
        Set<HTTPMethod> f = new HashSet<HTTPMethod>();
        f.add(HTTPMethod.GET);
        f.add(HTTPMethod.HEAD);
        f.add(HTTPMethod.OPTIONS);
        f.add(HTTPMethod.TRACE);
        safeMethods = Collections.unmodifiableSet(f);

        // ref http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.5.1 and
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.5.3
        // We are a transparent cache
        Set<String> headers = new HashSet<String>();
        headers.add("Connection");
        headers.add("Keep-Alive");
        headers.add("Proxy-Authenticate");
        headers.add("Proxy-Authorization");
        headers.add("TE");
        headers.add("Trailers");
        headers.add("Transfer-Encoding");
        headers.add("Upgrade");
        unmodifiableHeaders = Collections.unmodifiableSet(headers);
    }

    private final CacheStorage storage;
    private ResponseResolver resolver;

    public HTTPCache(CacheStorage storage, ResponseResolver resolver) {
        Validate.notNull(storage, "Cache storage may not be null");
        Validate.notNull(resolver, "Resolver may not be null");
        this.storage = storage;
        this.resolver = resolver;
    }

    public HTTPCache(CacheStorage storage) {
        Validate.notNull(storage, "Cache storage may not be null");
        this.storage = storage;
    }

    public void clear() {
        storage.clear();
    }

    public void setResolver(final ResponseResolver resolver) {
        Validate.isTrue(this.resolver == null, "You may not set the response resolver more than once.");
        Validate.notNull(resolver, "Resolver may not be null");
        this.resolver = resolver;
    }

    public CacheStorage getStorage() {
        return storage;
    }

    public HTTPResponse doCachedRequest(HTTPRequest request) {
        return doCachedRequest(request, false);
    }

    public HTTPResponse doCachedRequest(HTTPRequest request, boolean force) {
        if (resolver == null) {
            throw new IllegalStateException("The resolver was not set, no point of continuing with the request");
        }
        HTTPResponse response;
        if (!isCacheableRequest(request)) {
            if (!isSafeRequest(request)) {
                storage.invalidate(request.getRequestURI());
            }
            try {
                response = resolver.resolve(request);
            } catch (IOException e) {
                throw new HTTPException(e);
            }
        }
        else {
            //request is cacheable
            response = getFromCache(request, force);
        }
        if (response == null) {
            return new HTTPResponse(null, Status.INTERNAL_SERVER_ERROR, new Headers());
        }
        return response;
    }

    private HTTPResponse getFromCache(HTTPRequest request, final boolean force) {
        HTTPResponse response;
        if (!force) {
            CacheItem item = storage.get(request);
            if (item != null && item.isStale()) {
                //If the cached value is stale, execute the request and try to cache it.
                HTTPResponse staleResponse = item.getResponse();
                prepareConditionalRequest(request, staleResponse);

                response = handleResolve(request, item);
            }
            else if (item != null) {
                response = item.getResponse();
            }
            else {
                response = handleResolve(request, null);
            }
        }
        else {
            response = handleResolve(request, null);
        }

        return response;
    }

    private void prepareConditionalRequest(HTTPRequest request, HTTPResponse staleResponse) {
        Conditionals conditionals = request.getConditionals();
        if (request.getMethod() == HTTPMethod.GET && conditionals.toHeaders().isEmpty()) {
            if (staleResponse.getHeaders().hasHeader(ETAG)) {
                addIfNoneMatchHeader(staleResponse.getHeaders().getFirstHeader(ETAG), request);
            }
            else if (staleResponse.getLastModified() != null) {
                conditionals.setIfModifiedSince(staleResponse.getLastModified());
            }
        }
    }

    private HTTPResponse handleResolve(HTTPRequest request, CacheItem item) {
        HTTPResponse response = null;
        HTTPResponse resolvedResponse = null;
        try {
            resolvedResponse = resolver.resolve(request);
        } catch (IOException e) {
            //No cached item found, we throw an exception.
            if (item == null) {
                throw new HTTPException(e);
            }
            else {
                response = warn(item.getResponse(), e);
            }
        }
        if (resolvedResponse != null) {
            if (request.getMethod() == HTTPMethod.HEAD) {
                if (item != null) {
                    response = updateHeadersFromResolved(request, item, resolvedResponse);
                }
                else {
                    response = resolvedResponse;
                }
            }
            else if (isCacheableResponse(resolvedResponse)) {
                Vary vary = determineVariation(resolvedResponse, request);

                storage.put(request.getRequestURI(), vary, new CacheItem(resolvedResponse));
                response = resolvedResponse;
            }
            else if (item != null && resolvedResponse.getStatus().getCode() == Status.NOT_MODIFIED.getCode()) {
                response = updateHeadersFromResolved(request, item, resolvedResponse);
            }
            else {
                //Response was not cacheable
                response = resolvedResponse;
            }
            if (item != null && resolvedResponse.getStatus().getCode() == Status.OK.getCode()) {
                //Success was ok, but we had already a response for this item.
                //invalidate it so we don't clutter the filesystem.
                storage.invalidate(request.getRequestURI(), item);
            }
        }
        return response;
    }

    private HTTPResponse warn(HTTPResponse response, IOException e) {
        Headers headers = new Headers(response.getHeaders().getHeadersAsMap());
        headers.add(Warning.STALE_WARNING.toHeader());
        if (e instanceof SocketException) {
            headers.add(Warning.DISCONNECT_OPERATION_WARNING.toHeader());
        }
        return new HTTPResponse(response.getPayload(), Status.OK, headers);
    }

    private HTTPResponse updateHeadersFromResolved(HTTPRequest request, CacheItem item, HTTPResponse resolvedResponse) {
        HTTPResponse cachedResponse = item.getResponse();
        Map<String, List<Header>> headers = new LinkedHashMap<String, List<Header>>(cachedResponse.getHeaders().getHeadersAsMap());

        headers.putAll(removeUnmodifiableHeaders(resolvedResponse.getHeaders()).getHeadersAsMap());
        Headers realHeaders = new Headers(headers);
        HTTPResponse updatedResponse = new HTTPResponse(cachedResponse.getPayload(), resolvedResponse.getStatus(), realHeaders);
        Vary vary = determineVariation(updatedResponse, request);
        storage.put(request.getRequestURI(), vary, new CacheItem(updatedResponse));
        return updatedResponse;
    }

    private Headers removeUnmodifiableHeaders(Headers headers) {
        Headers washedHeaders = new Headers();
        Set<String> usableHeaders = new HashSet<String>(headers.keySet());
        usableHeaders.removeAll(unmodifiableHeaders);
        for (String removableHeader : usableHeaders) {
            if (headers.hasHeader(removableHeader)) {
                washedHeaders.put(removableHeader, headers.getHeaders((removableHeader)));
            }
        }
        return washedHeaders;
    }

    private Vary determineVariation(HTTPResponse resolvedResponse, HTTPRequest request) {
        Header varyHeader = resolvedResponse.getHeaders().getFirstHeader(VARY);
        Map<String, String> resolvedVaryHeaders = new HashMap<String, String>();
        if (varyHeader != null) {
            String[] varies = varyHeader.getValue().split(",");
            for (String vary : varies) {
                Header value = request.getHeaders().getFirstHeader(vary);
                resolvedVaryHeaders.put(vary, value == null ? null : value.getValue());
            }
        }
        return new Vary(resolvedVaryHeaders);
    }

    private boolean isCacheableResponse(HTTPResponse response) {
        if (response.getStatus().getCode() != Status.OK.getCode()) {
            return false;
        }
        Headers headers = response.getHeaders();
        return HeaderUtils.hasCacheableHeaders(headers);

    }

    private void addIfNoneMatchHeader(final Header eTagHeader, HTTPRequest request) {
        Tag tag = eTagHeader == null ? null : Tag.parse(eTagHeader.getValue());
        if (tag != null && tag != Tag.ALL) {
            request.getConditionals().addIfNoneMatch(tag);
        }
    }

    private boolean isSafeRequest(HTTPRequest request) {
        return safeMethods.contains(request.getMethod());
    }

    private boolean isCacheableRequest(HTTPRequest request) {
        if (request.getMethod() == HTTPMethod.GET || request.getMethod() == HTTPMethod.HEAD) {
            if (request.getHeaders().hasHeader(HeaderConstants.CACHE_CONTROL)) {
                String cacheControlHeaderValue = request.getHeaders().getFirstHeader(HeaderConstants.CACHE_CONTROL).getValue();
                //If the request tells us that we shouldn't cache the response, then we don't.
                return !cacheControlHeaderValue.contains("no-store") || !cacheControlHeaderValue.contains("no-cache");
            }
            return true;
        }
        return false;
    }
}