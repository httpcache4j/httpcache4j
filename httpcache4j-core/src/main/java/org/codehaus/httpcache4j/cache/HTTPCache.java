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
 * Support Warning header http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46 partly supported now...
 * Support Range headers. http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35
 *
 */

/**
 * The main HTTPCache class.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class HTTPCache {
    /**
     * Directive to the {@code "Cache-Control"} header
     */
    public static final String HEADER_DIRECTIVE_MAX_AGE = "max-age";

    private final HTTPCacheHelper helper = new HTTPCacheHelper();
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
        if (!helper.isCacheableRequest(request)) {
            if (!helper.isSafeRequest(request)) {
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
                if (item.isStale()) {
                    //If the cached value is stale, execute the request and try to cache it.
                    HTTPResponse staleResponse = item.getResponse();
                    helper.prepareConditionalRequest(request, staleResponse);

                    response = handleResolve(request, item);
                }
                else {
                    response = item.getResponse();
                }
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
                response = helper.warn(item.getResponse(), e);
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
            else if (helper.isCacheableResponse(resolvedResponse)) {
                Vary vary = helper.determineVariation(resolvedResponse, request);

                storage.put(request.getRequestURI(), vary, new CacheItem(resolvedResponse));
                response = resolvedResponse;
            }
            else if (item != null && resolvedResponse.getStatus().getCode() == Status.NOT_MODIFIED.getCode()) {
                response = updateHeadersFromResolved(request, item, resolvedResponse);
            }
            else if (item != null && resolvedResponse.getStatus().getCode() == Status.OK.getCode()) {
                //Success was ok, but we had already a response for this item.
                //invalidate it so we don't clutter the filesystem.
                storage.invalidate(request.getRequestURI(), item);
            }
            else {
                //Response was not cacheable
                response = resolvedResponse;
            }
        }
        return response;
    }

    private HTTPResponse updateHeadersFromResolved(HTTPRequest request, CacheItem item, HTTPResponse resolvedResponse) {
        HTTPResponse cachedResponse = item.getResponse();
        Map<String, List<Header>> headers = new LinkedHashMap<String, List<Header>>(cachedResponse.getHeaders().getHeadersAsMap());

        headers.putAll(helper.removeUnmodifiableHeaders(resolvedResponse.getHeaders()).getHeadersAsMap());
        Headers realHeaders = new Headers(headers);
        realHeaders.add(HeaderConstants.AGE, helper.calculateAge(resolvedResponse, cachedResponse));
        HTTPResponse updatedResponse = new HTTPResponse(cachedResponse.getPayload(), resolvedResponse.getStatus(), realHeaders);
        Vary vary = helper.determineVariation(updatedResponse, request);
        storage.put(request.getRequestURI(), vary, new CacheItem(updatedResponse));
        return updatedResponse;
    }
}