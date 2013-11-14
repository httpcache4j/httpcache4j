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
import org.codehaus.httpcache4j.resolver.ResponseResolver;
import org.codehaus.httpcache4j.uri.URIBuilder;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.URI;

/**
 * The main HTTPCache class.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class HTTPCache {
    private final HTTPCacheHelper helper;
    private final CacheStatistics statistics = new CacheStatistics();
    private final CacheStorage storage;
    private final ResponseResolver resolver;
    private final Mutex<URI> mutex = new Mutex<URI>();
    private boolean translateHEADToGET = false;

    public HTTPCache(CacheStorage storage, ResponseResolver resolver) {
        this.storage = Preconditions.checkNotNull(storage, "Cache storage may not be null");
        this.resolver = Preconditions.checkNotNull(resolver, "Resolver may not be null");
        helper = new HTTPCacheHelper(CacheHeaderBuilder.getBuilder());
    }

    public void clear() {
        storage.clear();
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

    public HTTPResponse execute(final HTTPRequest request) {
        return execute(request, helper.isEndToEndReloadRequest(request));
    }

    public HTTPResponse executeRefresh(final HTTPRequest request) {
        return execute(request, true);
    }

    public void shutdown() {
        storage.shutdown();
        resolver.shutdown();
    }

    private HTTPResponse execute(final HTTPRequest request, boolean force) {
        HTTPResponse response;
        if (!helper.isCacheableRequest(request)) {
            response = unconditionalResolve(request);
        } else {
            //request is cacheable
            boolean shouldUnlock = true;
            try {
                force = force || request.getMethod() == HTTPMethod.OPTIONS || request.getMethod() == HTTPMethod.TRACE;
                if (mutex.acquire(request.getRequestURI())) {
                    response = doRequest(request, force || (request.getHeaders().getCacheControl() != null && request.getHeaders().getCacheControl().isNoStore()));
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

    private HTTPResponse doRequest(HTTPRequest request, final boolean force) {
        if (request.getMethod() == HTTPMethod.HEAD && isTranslateHEADToGET()) {
            request = request.method(HTTPMethod.GET);
        }
        HTTPResponse response;
        if (force) {
            response = unconditionalResolve(request);
        } else {
            response = getFromStorage(request);
        }
        return response;
    }

    private HTTPResponse getFromStorage(HTTPRequest request) {
        final DateTime requestTime = DateTime.now();
        HTTPResponse response;
        final CacheItem item = storage.get(request);
        if (item != null) {
            statistics.hit();
            if (item.isStale(requestTime)) {
                //If the cached value is stale, execute the request and try to cache it.
                HTTPResponse staleResponse = item.getResponse();
                //If the payload has been deleted for some reason, we want to do a unconditional GET
                HTTPRequest conditionalRequest = maybePrepareConditionalResponse(request, staleResponse);
                response = handleStaleResponse(conditionalRequest, request, item, requestTime);
            } else {
                response = helper.rewriteResponse(request, item.getResponse(), item.getAge(requestTime));
            }
        } else {
            statistics.miss();
            response = unconditionalResolve(request);
        }
        return response;
    }

    private HTTPResponse handleStaleResponse(HTTPRequest conditionalRequest, HTTPRequest originalRequest, CacheItem item, DateTime requestTime) {
        int age = item.getAge(DateTime.now());
        if (!helper.allowStale(item, originalRequest, requestTime)) {
            HTTPResponse response = excuteImpl(conditionalRequest, item);
            return helper.rewriteResponse(originalRequest, response, age);
        }
        return helper.rewriteStaleResponse(originalRequest, item.getResponse(), age);
    }

    private HTTPRequest maybePrepareConditionalResponse(HTTPRequest request, HTTPResponse staleResponse) {
        if (!staleResponse.hasPayload() || staleResponse.getPayload().isAvailable()) {
            return helper.prepareConditionalGETRequest(request, staleResponse);
        }
        return request.headers(request.getHeaders().withConditionals(new Conditionals()));
    }

    private HTTPResponse unconditionalResolve(final HTTPRequest request) {
        return helper.rewriteResponse(request, excuteImpl(request, null), -1);
    }

    private HTTPResponse excuteImpl(final HTTPRequest request, final CacheItem item) {
        HTTPResponse response = null;
        HTTPResponse resolvedResponse = null;
        try {
            resolvedResponse = resolver.resolve(request);
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
            boolean updated = false;

            if (isInvalidating(request, resolvedResponse, item)) {
                response = resolvedResponse;
                URI requestUri = request.getNormalizedURI();
                storage.invalidate(requestUri);
                updated = true;

                if (!request.getMethod().isSafe()) {
                    // http://tools.ietf.org/html/rfc2616#section-13.10
                    invalidateIfSameHostAsRequest(resolvedResponse.getLocation(), requestUri);
                    invalidateIfSameHostAsRequest(resolvedResponse.getContentLocation(), requestUri);
                }
            }
            else if (helper.isCacheableResponse(resolvedResponse) && helper.shouldBeStored(resolvedResponse)) {
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

    //http://tools.ietf.org/html/rfc2616#section-9.4
    private boolean isInvalidatingHEADResponse(HTTPRequest request, CacheItem item, HTTPResponse resolvedResponse) {
        return request.getMethod() == HTTPMethod.HEAD && item != null && resolvedResponse.getStatus() != Status.NOT_MODIFIED;
    }

    private boolean isInvalidating(HTTPRequest request, HTTPResponse resolvedResponse, CacheItem item) {
        boolean invalidatingHEAD = isInvalidatingHEADResponse(request, item, resolvedResponse);
        boolean unsafe = !request.getMethod().isSafe() && isSuccessfulResponseToUnsafeRequest(resolvedResponse);
        return unsafe || invalidatingHEAD;
    }

    //http://tools.ietf.org/html/draft-ietf-httpbis-p6-cache-22#section-6
    private boolean isSuccessfulResponseToUnsafeRequest(HTTPResponse resolvedResponse) {
        Status.Category category = resolvedResponse.getStatus().getCategory();
        return category == Status.Category.SUCCESS || category == Status.Category.REDIRECTION;
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

    private void invalidateIfSameHostAsRequest(URI uri, URI requestUri) {
        if (uri != null && uri.getHost() != null && uri.getHost().equals(requestUri.getHost())) {
            storage.invalidate(URIBuilder.fromURI(uri).toNormalizedURI());
        }
    }
}
