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
import org.codehaus.httpcache4j.uri.URIBuilder;
import org.codehaus.httpcache4j.util.NamedThreadFactory;
import org.codehaus.httpcache4j.util.OptionalUtils;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * The main HTTPCache class.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class HTTPCache implements AutoCloseable {
    private final HTTPCacheHelper helper;
    private final CacheStatistics statistics = new CacheStatistics();
    private final CacheStorage storage;
    private final ResponseResolver resolver;
    private final Mutex<URI> mutex = new Mutex<>();
    private final ExecutorService executor;
    private boolean translateHEADToGET = false;

    public HTTPCache(CacheStorage storage, ResponseResolver resolver) {
        this(storage, resolver, 4);
    }

    public HTTPCache(CacheStorage storage, ResponseResolver resolver, int nThreads) {
        this(storage, resolver, Executors.newFixedThreadPool(nThreads, new NamedThreadFactory("HttpCache4j", true)));
    }

    public HTTPCache(CacheStorage storage, ResponseResolver resolver, ExecutorService executor) {
        this.storage = Objects.requireNonNull(storage, "Cache storage may not be null");
        this.resolver = Objects.requireNonNull(resolver, "Resolver may not be null");
        this.helper = new HTTPCacheHelper(CacheHeaderBuilder.getBuilder());
        this.executor = executor;
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
        return executeAsync(request).join();
    }

    public CompletableFuture<HTTPResponse> executeAsync(final HTTPRequest request) {
        return execute(request, helper.isEndToEndReloadRequest(request));
    }

    public HTTPResponse executeRefresh(final HTTPRequest request) {
        return executeRefreshAsync(request).join();
    }

    public CompletableFuture<HTTPResponse> executeRefreshAsync(final HTTPRequest request) {
        return execute(request, true);
    }

    public void shutdown() {
        executor.shutdown();
        storage.shutdown();
        resolver.shutdown();
    }

    @Override
    public void close() throws Exception {
        shutdown();
    }

    private CompletableFuture<HTTPResponse> execute(final HTTPRequest request, boolean force) {
        if (!helper.isCacheableRequest(request)) {
            return unconditionalResolve(request);
        } else {
            final CompletableFuture<HTTPResponse> res = new CompletableFuture<>();
            //request is cacheable
            executor.execute(() -> {
                boolean shouldUnlock = true;
                try {
                    CompletableFuture<HTTPResponse> future;
                    boolean forces = force || request.getMethod() == HTTPMethod.OPTIONS || request.getMethod() == HTTPMethod.TRACE;
                    if (mutex.acquire(request.getNormalizedURI())) {
                        future = doRequest(request, forces || (OptionalUtils.exists(request.getHeaders().getCacheControl(), CacheControl::isNoStore)));
                    } else {
                        future = completedFuture(new HTTPResponse(null, Status.BAD_GATEWAY, new Headers()));
                        shouldUnlock = false;
                    }

                    future.whenComplete((response, ex) -> {
                        if (ex != null) {
                            res.completeExceptionally(ex);
                        } else {
                            res.complete(response);
                        }
                    });
                } finally {
                    if (shouldUnlock) {
                        mutex.release(request.getNormalizedURI());
                    }
                }

            });

            return res;
        }
    }

    private CompletableFuture<HTTPResponse> doRequest(HTTPRequest request, final boolean force) {
        if (request.getMethod() == HTTPMethod.HEAD && isTranslateHEADToGET()) {
            request = request.withMethod(HTTPMethod.GET);
        }
        CompletableFuture<HTTPResponse> response;
        if (force) {
            response = unconditionalResolve(request);
        } else {
            response = getFromStorage(request);
        }
        return response;
    }

    private CompletableFuture<HTTPResponse> getFromStorage(HTTPRequest request) {
        final LocalDateTime requestTime = LocalDateTime.now();
        CompletableFuture<HTTPResponse> response;
        final CacheItem item = storage.get(request);
        if (item != null) {
            statistics.hit();
            final HTTPResponse cachedResponse = item.getResponse();
            boolean mustRevalidate = cachedResponse.getHeaders().getCacheControl().orElse(CacheControl.empty()).isMustRevalidate();
            if (mustRevalidate || item.isStale(requestTime)) {
                //If the cached value is stale, execute the request and try to cache it.
                //If the payload has been deleted for some reason, we want to do a unconditional GET
                HTTPRequest conditionalRequest = maybePrepareConditionalResponse(request, cachedResponse);
                response = handleStaleResponse(conditionalRequest, request, item, requestTime);
            } else {
                response = completedFuture(helper.rewriteResponse(request, cachedResponse, item.getAge(requestTime)));
            }
        } else {
            statistics.miss();
            response = unconditionalResolve(request);
        }
        return response;
    }

    private CompletableFuture<HTTPResponse> handleStaleResponse(HTTPRequest conditionalRequest, HTTPRequest originalRequest, CacheItem item, LocalDateTime requestTime) {
        long age = item.getAge(LocalDateTime.now());
        if (!helper.allowStale(item, originalRequest, requestTime)) {
            return executeImpl(conditionalRequest, item).thenApply(res -> helper.rewriteResponse(originalRequest, res, age));
        }
        return completedFuture(helper.rewriteStaleResponse(originalRequest, item.getResponse(), age));
    }

    private HTTPRequest maybePrepareConditionalResponse(HTTPRequest request, HTTPResponse staleResponse) {
        if (!staleResponse.hasPayload() || staleResponse.getPayload().get().isAvailable()) {
            return helper.prepareConditionalGETRequest(request, staleResponse);
        }
        return request.headers(request.getHeaders().withConditionals(new Conditionals()));
    }

    private CompletableFuture<HTTPResponse> unconditionalResolve(final HTTPRequest request) {
        return executeImpl(request, null).thenApply(res -> helper.rewriteResponse(request, res, -1));
    }

    private CompletableFuture<HTTPResponse> executeImpl(final HTTPRequest request, final CacheItem item) {
        CompletableFuture<HTTPResponse> resolvedResponse = resolver.resolve(request);

        return resolvedResponse.thenCompose(resolved -> handleResolved(request, item, resolved)).handle((res, ex) -> {
            if (ex != null) {
                if (item == null) {
                    throw new HTTPException(ex);
                } else {
                    Headers headers = helper.warn(item.getResponse().getHeaders(), ex);
                    return item.getResponse().withHeaders(headers);
                }
            } else {
                return res;
            }
        });
    }

    private CompletableFuture<HTTPResponse> handleResolved(HTTPRequest request, CacheItem item, HTTPResponse resolved) {
        final CompletableFuture<HTTPResponse> responseFuture = new CompletableFuture<>();

        boolean updated = false;

        if (isInvalidating(request, resolved, item)) {
            responseFuture.complete(resolved);
            URI requestUri = request.getNormalizedURI();
            storage.invalidate(requestUri);
            updated = true;

            if (!request.getMethod().isSafe()) {
                // http://tools.ietf.org/html/rfc2616#section-13.10
                invalidateIfSameHostAsRequest(resolved.getHeaders().getLocation(), requestUri);
                invalidateIfSameHostAsRequest(resolved.getHeaders().getContentLocation(), requestUri);
            }
        } else if (helper.isCacheableResponse(resolved) && helper.shouldBeStored(resolved)) {
            responseFuture.complete(storage.insert(request, resolved));
            updated = true;

        } else {
            //Response could not be cached
            responseFuture.complete(resolved);
        }
        final boolean theUpdate = updated;

        return responseFuture.thenApply(response -> {
            if (item != null) {
                //from http://tools.ietf.org/html/rfc2616#section-13.5.3
                if (resolved.getStatus() == Status.NOT_MODIFIED || resolved.getStatus() == Status.PARTIAL_CONTENT) {
                    return updateHeadersFromResolved(request, item, resolved);
                } else if (theUpdate) {
                    Headers newHeaders = response.getHeaders().add(CacheHeaderBuilder.getBuilder().createMISSXCacheHeader());
                    return response.withHeaders(newHeaders);
                }
            }
            return response;
        });
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

    private void invalidateIfSameHostAsRequest(Optional<URI> uri, URI requestUri) {
        if (uri.isPresent() && uri.get().getHost() != null && uri.get().getHost().equals(requestUri.getHost())) {
            storage.invalidate(URIBuilder.fromURI(uri.get()).toNormalizedURI());
        }
    }
}
