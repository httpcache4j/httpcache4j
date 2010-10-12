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

import java.net.UnknownHostException;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.resolver.ResponseResolver;

import org.apache.commons.lang.Validate;

import java.io.IOException;
import java.net.InetAddress;
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
    private final HTTPCacheHelper helper = new HTTPCacheHelper();
    private final CacheStatistics statistics = new CacheStatistics();
    private final CacheStorage storage;
    private ResponseResolver resolver;
    private final Mutex<URI> mutex = new Mutex<URI>();

    public HTTPCache(CacheStorage storage, ResponseResolver resolver) {
        Validate.notNull(storage, "Cache storage may not be null");
        this.storage = storage;
        this.resolver = resolver;
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
            HTTPRequest req = request;
            if (item != null) {
                statistics.hit();
                if (item.isStale(req)) {
                    //If the cached value is stale, execute the request and try to cache it.
                    HTTPResponse staleResponse = item.getResponse();
                    //If the payload has been deleted for some reason, we want to do a unconditional GET
                    req = maybePrepareConditionalResponse(request, staleResponse);
                    response = rewriteResponse(request, item, req);
                }
                else {
                    response = helper.rewriteResponse(request, item);
                    response = addCacheHitStatHeader(response);
                }
            }
            else {
                statistics.miss();
                response = unconditionalResolve(req);
                response = addCacheMissStatHeader(response);
            }
        }
        return response;
    }

    private HTTPResponse rewriteResponse(HTTPRequest request, CacheItem item, HTTPRequest req) {
        if (!helper.allowStale(item, req)) {
            return handleResolve(req, item);
        }
        return helper.rewriteStaleResponse(request, item);
    }

    private HTTPRequest maybePrepareConditionalResponse(HTTPRequest request, HTTPResponse staleResponse) {
        if (!staleResponse.hasPayload() || staleResponse.getPayload().isAvailable()) {
            return helper.prepareConditionalRequest(request, staleResponse);
        }
        return request.conditionals(new Conditionals());
    }

    private HTTPResponse unconditionalResolve(final HTTPRequest request) {
        return handleResolve(request, null);
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
                response = helper.warn(item.getResponse(), e);
                response = addCacheHitStatHeader(response);
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
                //Response was not cacheable
                response = resolvedResponse;
            }

            if (item != null) {
                if (resolvedResponse.getStatus() == Status.NOT_MODIFIED) {
                    response = updateHeadersFromResolved(request, item, resolvedResponse);
                }
                else {
                    response = addCacheMissStatHeader(response);
                }
            }
            else {
                response = addCacheMissStatHeader(response);
            }
        }
        return response;
    }

    protected HTTPResponse updateHeadersFromResolved(final HTTPRequest request, final CacheItem item, final HTTPResponse resolvedResponse) {
        HTTPResponse cachedResponse = item.getResponse();
        Headers headers = new Headers(cachedResponse.getHeaders());
        final Headers removeUnmodifiableHeaders = helper.removeUnmodifiableHeaders(resolvedResponse.getHeaders());
        if(removeUnmodifiableHeaders.hasHeader(HeaderConstants.DATE) && headers.hasHeader(HeaderConstants.DATE)) {
            headers = headers.remove(HeaderConstants.DATE);
        }
        headers = headers.add(removeUnmodifiableHeaders);
        HTTPResponse updatedResponse = new HTTPResponse(cachedResponse.getPayload(), cachedResponse.getStatus(), headers);
        updatedResponse = storage.update(request, updatedResponse);
        updatedResponse = addCacheHitStatHeader(updatedResponse);
        return updatedResponse;
    }

    protected String getHitString() {
        String canonicalHostName;
        try {
            canonicalHostName = InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (UnknownHostException ex) {
            canonicalHostName = "Unknown";
        }
        final String hitString = new StringBuilder("HIT from ").append(canonicalHostName).toString();
        return hitString;
    }

    protected String getMissString() {
        String canonicalHostName;
        try {
            canonicalHostName = InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (UnknownHostException ex) {
            canonicalHostName = "Unknown";
        }
        final String hitString = new StringBuilder("MISS from ").append(canonicalHostName).toString();
        return hitString;
    }

    protected HTTPResponse addCacheHitStatHeader(HTTPResponse response) {
          String hitString = getHitString();
          return addCacheStatHeader(hitString, response);
    }

    protected HTTPResponse addCacheMissStatHeader(HTTPResponse response) {
        String missString = getMissString();
        return addCacheStatHeader(missString, response);
    }

    protected HTTPResponse addCacheStatHeader(String hitString, HTTPResponse response) {
        Header header = new Header(HeaderConstants.CUSTOM_HTTPCACHE4J_HEADER, hitString);
        Headers headers = response.getHeaders().add(header);
        response = new HTTPResponse(response.getPayload(), response.getStatusLine(), headers);
        return response;
    }
}