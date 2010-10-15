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
import org.codehaus.httpcache4j.resolver.ResponseResolver;

import org.apache.commons.lang.Validate;

import java.io.IOException;
import java.net.URI;

/**
 * TODO:
 * Support Warning header http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46 partly supported now...
 * Support Range headers. http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35
 *
 */

/**
 * The main HTTPCache class.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class HTTPCache {
    private final HTTPCacheHelper helper;
    private final CacheStatistics statistics = new CacheStatistics();
    private final CacheStorage storage;
    private ResponseResolver resolver;
    private final Mutex<URI> mutex = new Mutex<URI>();

    public HTTPCache(CacheStorage storage, ResponseResolver resolver) {
        Validate.notNull(storage, "Cache storage may not be null");
        this.storage = storage;
        this.resolver = resolver;
        helper = new HTTPCacheHelper(CacheHeaderBuilder.getBuilder());
    }

    public HTTPCache(CacheStorage storage) {
        this(storage, null);
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

    public ResponseResolver getResolver() {
        return resolver;
    }

    public CacheStatistics getStatistics() {
        return statistics;
    }

    public HTTPResponse doCachedRequest(final HTTPRequest request) {
        return doCachedRequest(request, false);
    }

    public HTTPResponse doCachedRequest(final HTTPRequest request, boolean force) {
        if (resolver == null) {
            throw new IllegalStateException("The resolver was not set, no point of continuing with the request");
        }
        HTTPResponse response;
        if (!helper.isCacheableRequest(request)) {
            if (!helper.isSafeRequest(request)) {
                storage.invalidate(request.getRequestURI());
            }
            response = unconditionalResolve(request);
        }
        else {
            //request is cacheable
            mutex.acquire(request.getRequestURI());
            try {
                response = getFromCache(request, force || request.getConditionals().isUnconditional());
            } finally {
                mutex.release(request.getRequestURI());
            }
        }
        if (response == null) {
            throw new HTTPException("No response produced");
        }
        return response;
    }

    private HTTPResponse getFromCache(final HTTPRequest request, final boolean force) {
        HTTPResponse response;
        if (force) {
            response = unconditionalResolve(request);
        }
        else {                    
            CacheItem item = storage.get(request);
            if (item != null) {
                statistics.hit();
                if (item.isStale(request)) {
                    //If the cached value is stale, execute the request and try to cache it.
                    HTTPResponse staleResponse = item.getResponse();
                    //If the payload has been deleted for some reason, we want to do a unconditional GET
                    HTTPRequest conditionalRequest = maybePrepareConditionalResponse(request, staleResponse);
                    response = handleStaleResponse(conditionalRequest, request, item);
                }
                else {
                    response = helper.rewriteResponse(request, item.getResponse(), item.getAge(request));
                }
            }
            else {
                statistics.miss();
                response = unconditionalResolve(request);
            }
        }
        return response;
    }

    private HTTPResponse handleStaleResponse(HTTPRequest conditionalRequest, HTTPRequest originalRequest, CacheItem item) {
        int age = item.getAge(conditionalRequest);
        if (!helper.allowStale(item, originalRequest)) {
            HTTPResponse response = handleResolve(conditionalRequest, item);
            return helper.rewriteResponse(originalRequest, response, age);
        }
        return helper.rewriteStaleResponse(originalRequest, item.getResponse(), age);
    }

    private HTTPRequest maybePrepareConditionalResponse(HTTPRequest request, HTTPResponse staleResponse) {
        if (!staleResponse.hasPayload() || staleResponse.getPayload().isAvailable()) {
            return helper.prepareConditionalRequest(request, staleResponse);
        }
        return request.conditionals(new Conditionals());
    }

    private HTTPResponse unconditionalResolve(final HTTPRequest request) {
        return helper.rewriteResponse(request, handleResolve(request, null), -1);
    }

    private HTTPResponse handleResolve(final HTTPRequest request, final CacheItem item) {
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
                Headers headers = helper.warn(item.getResponse().getHeaders(), e);
                response = new HTTPResponse(item.getResponse().getPayload(), item.getResponse().getStatusLine(), headers);
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
                response = storage.insert(request, resolvedResponse);
            }
            else {
                //Response could not be cached
                response = resolvedResponse;
            }

            if (item != null) {
                if (resolvedResponse.getStatus() == Status.NOT_MODIFIED) {
                    response = updateHeadersFromResolved(request, item, resolvedResponse);
                }
            }
        }
        return response;
    }

    protected HTTPResponse updateHeadersFromResolved(final HTTPRequest request, final CacheItem item, final HTTPResponse resolvedResponse) {
        HTTPResponse cachedResponse = item.getResponse();
        Headers headers = new Headers(cachedResponse.getHeaders());
        final Headers removeUnmodifiableHeaders = helper.removeUnmodifiableHeaders(resolvedResponse.getHeaders());
        headers = headers.add(removeUnmodifiableHeaders);
        HTTPResponse updatedResponse = new HTTPResponse(cachedResponse.getPayload(), cachedResponse.getStatus(), headers);
        updatedResponse = storage.update(request, updatedResponse);
        return updatedResponse;
    }
}
