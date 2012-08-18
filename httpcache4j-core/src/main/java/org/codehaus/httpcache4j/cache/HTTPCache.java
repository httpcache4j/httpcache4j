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

import com.google.common.base.Preconditions;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.mutable.MutableRequest;
import org.codehaus.httpcache4j.resolver.ResponseResolver;

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
    private boolean translateHEADToGET = false;

    public HTTPCache(CacheStorage storage, ResponseResolver resolver) {
        this.storage = Preconditions.checkNotNull(storage, "Cache storage may not be null");
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
        Preconditions.checkArgument(this.resolver == null, "You may not set the response resolver more than once.");
        this.resolver = Preconditions.checkNotNull(resolver, "Resolver may not be null");
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

    @Deprecated
    public HTTPResponse doCachedRequest(final HTTPRequest request) {
        return execute(request, false);
    }

    @Deprecated
    public HTTPResponse refreshCachedRequest(final HTTPRequest request) {
        return execute(request, true);
    }

    public HTTPResponse execute(final HTTPRequest request) {
        return execute(request, false);
    }

    public HTTPResponse executeRefresh(final HTTPRequest request) {
        return execute(request, true);
    }

    @Deprecated
    public HTTPResponse doCachedRequest(final HTTPRequest request, boolean force) {
        return execute(request, force);
    }

    public HTTPResponse execute(final HTTPRequest request, boolean force) {
        if (resolver == null) {
            throw new IllegalStateException("The resolver was not set, no point of continuing with the request");
        }
        HTTPResponse response;
        if (!helper.isCacheableRequest(request)) {
            response = unconditionalResolve(request);
        } else {
            //request is cacheable
            boolean shouldUnlock = true;
            try {
                if (mutex.acquire(request.getRequestURI())) {
                    response = doRequest(request, force || request.getConditionals().isUnconditional());
                } else {
                    response = new HTTPResponse(null, Status.BAD_GATEWAY, new Headers());
                    shouldUnlock = false;
                }
            } finally {
                if (shouldUnlock) {
                    mutex.release(request.getRequestURI());
                }
            }
        }
        if (response == null) {
            throw new HTTPException("No response produced");
        }
        return response;
    }

    private HTTPResponse doRequest(final HTTPRequest request, final boolean force) {
        HTTPResponse response;
        if (force) {
            response = unconditionalResolve(request);
        } else {
            response = getFromStorage(request);
        }
        return response;
    }

    private HTTPResponse getFromStorage(HTTPRequest request) {
        HTTPResponse response;
        CacheItem item = storage.get(request);
        if (item != null) {
            statistics.hit();
            if (item.isStale(request)) {
                //If the cached value is stale, execute the request and try to cache it.
                HTTPResponse staleResponse = item.getResponse();
                //If the payload has been deleted for some reason, we want to do a unconditional GET
                HTTPRequest conditionalRequest = maybePrepareConditionalResponse(request, staleResponse);
                response = handleStaleResponse(conditionalRequest, request, item);
            } else {
                response = helper.rewriteResponse(request, item.getResponse(), item.getAge(request));
            }
        } else {
            statistics.miss();
            response = unconditionalResolve(request);
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
            return helper.prepareConditionalGETRequest(request, staleResponse);
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
            resolvedResponse = resolveWithHeadRewrite(request, resolvedResponse);
        } catch (IOException e) {
            //No cached item found, we throw an exception.
            if (item == null) {
                throw new HTTPException(e);
            } else {
                Headers headers = helper.warn(item.getResponse().getHeaders(), e);
                response = item.getResponse().withHeaders(headers);
            }
        }
        if (resolvedResponse != null) {
            if (!request.getMethod().isSafe() && resolvedResponse.getStatus().getCategory() == Status.Category.SUCCESS) {
                storage.invalidate(request.getRequestURI());
            }

            boolean updated = false;

            if (request.getMethod() == HTTPMethod.HEAD && !isTranslateHEADToGET()) {
                if (item != null) {
                    response = updateHeadersFromResolved(request, item, resolvedResponse);
                } else {
                    response = resolvedResponse;
                }
            } else if (helper.isCacheableResponse(resolvedResponse) && helper.shouldBeStored(resolvedResponse)) {
                response = storage.insert(request, resolvedResponse);
                updated = true;

            } else {
                //Response could not be cached
                response = resolvedResponse;
            }
            if (item != null) {
                //from http://tools.ietf.org/html/rfc2616#section-13.5.3
                if (resolvedResponse.getStatus() == Status.NOT_MODIFIED || resolvedResponse.getStatus() == Status.PARTIAL_CONTENT) {
                    response = updateHeadersFromResolved(request, item, resolvedResponse);
                } else if (updated) {
                    Headers newHeaders = response.getHeaders().add(CacheHeaderBuilder.getBuilder().createMISSXCacheHeader());
                    response = response.withHeaders(newHeaders);
                }
            }
        }
        return response;
    }

    private HTTPResponse resolveWithHeadRewrite(HTTPRequest request, HTTPResponse resolvedResponse) throws IOException {
        if (request.getMethod() == HTTPMethod.HEAD && isTranslateHEADToGET()) { // We change this to GET and cache the result.
            resolvedResponse = resolver.resolve(request.method(HTTPMethod.GET));
        } else {
            resolvedResponse = resolver.resolve(request);
        }
        return resolvedResponse;
    }

    HTTPResponse updateHeadersFromResolved(final HTTPRequest request, final CacheItem item, final HTTPResponse resolvedResponse) {
        HTTPResponse cachedResponse = item.getResponse();
        Headers headers = new Headers(cachedResponse.getHeaders());
        Headers headersToBeSet = helper.removeUnmodifiableHeaders(resolvedResponse.getHeaders());
        HTTPResponse updatedResponse = cachedResponse.withHeaders(headers.set(headersToBeSet));
        return storage.update(request, updatedResponse);
    }

    public boolean isTranslateHEADToGET() {
        return translateHEADToGET;
    }

    public void setTranslateHEADToGET(boolean translateHEADToGET) {
        this.translateHEADToGET = translateHEADToGET;
    }
}
