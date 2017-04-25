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

package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.util.OptionalUtils;

import java.io.IOException;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
class HTTPCacheHelper {
    private static final Set<String> unmodifiableHeaders;
    private static final Set<Status> cacheableStatuses;

    static {
        // ref http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.5.1 and
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.5.3
        // We are a transparent cache
        Set<String> headers = new HashSet<>();
        headers.add("Connection");
        headers.add("Keep-Alive");
        headers.add("Proxy-Authenticate");
        headers.add("Proxy-Authorization");
        headers.add("TE");
        headers.add("Trailers");
        headers.add("Transfer-Encoding");
        headers.add("Upgrade");
        unmodifiableHeaders = Collections.unmodifiableSet(headers);

        /**
         * 200, 203, 206, 300, 301 or 410
         * We do not handle ranges, so we dont support 206.
         */
        cacheableStatuses = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                Status.OK,
                Status.NON_AUTHORITATIVE_INFORMATION,
                Status.MULTIPLE_CHOICES,
                Status.MOVED_PERMANENTLY,
                Status.GONE
        )));
    }

    private CacheHeaderBuilder cacheHeaderBuilder;

    HTTPCacheHelper(CacheHeaderBuilder cacheHeaderBuilder) {
        this.cacheHeaderBuilder = cacheHeaderBuilder;
    }

    Headers warn(Headers headers, IOException e) {
        headers = headers.add(Warning.STALE_WARNING.toHeader());
        if (e instanceof SocketException) {
            headers = headers.add(Warning.DISCONNECT_OPERATION_WARNING.toHeader());
        }
        return headers;
    }

    Headers removeUnmodifiableHeaders(Headers headers) {
        Headers washedHeaders = new Headers();
        Set<String> usableHeaders = new HashSet<>(headers.keySet());
        usableHeaders.removeAll(unmodifiableHeaders);
        for (String headerName : usableHeaders) {
            if (headers.contains(headerName)) {
                washedHeaders = washedHeaders.add(headers.getHeaders((headerName)));
            }
        }
        return washedHeaders;
    }

    /**
     * A response received with a status code of 200, 203, 206, 300, 301 or 410 MAY be stored by a cache and used in reply to a subsequent request,
     * subject to the expiration mechanism, unless a cache-control directive prohibits caching.
     * However, a cache that does not support the Range and Content-Range headers MUST NOT cache 206 (Partial Content) responses.
     *
     * We do not support 206 (Partial Content).
     * See: http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.4
     * @param response the response to analyze
     * @return {@code true} if the response is cacheable. {@code false} if not.
     */
    boolean isCacheableResponse(HTTPResponse response) {
        if (!cacheableStatuses.contains(response.getStatus())) {
            return false;
        }
        Headers headers = response.getHeaders();
        return headers.isCachable();
    }

    HTTPRequest prepareConditionalGETRequest(HTTPRequest request, HTTPResponse staleResponse) {
        Conditionals conditionals = request.getHeaders().getConditionals();
        if (request.getMethod() == HTTPMethod.GET && conditionals.toHeaders().isEmpty()) {
            if (staleResponse.getHeaders().getETag().isPresent()) {
                conditionals = new Conditionals().addIfNoneMatch(staleResponse.getHeaders().getETag().get());
            }
            else if (staleResponse.getHeaders().getLastModified().isPresent()) {
                conditionals = conditionals.ifModifiedSince(staleResponse.getHeaders().getLastModified().get());
            }
            return request.headers(request.getHeaders().withConditionals(conditionals));
        }
        return request;
    }

    Headers warnStale(Headers headers) {
        return warn(headers, null);
    }

    boolean isEndToEndReloadRequest(HTTPRequest request) {
        Optional<CacheControl> cacheControl = request.getCacheControl();
        return request.getMethod().isCacheable() && OptionalUtils.exists(cacheControl, CacheControl::isNoCache);
    }

    boolean isCacheableRequest(HTTPRequest request) {
        if (request.getMethod().isCacheable()) {
            Optional<CacheControl> cc = request.getCacheControl();
            return OptionalUtils.forall(cc, c -> !c.isNoCache() && !c.isNoStore());
        }
        return false;
    }

    boolean allowStale(CacheItem item, HTTPRequest req, LocalDateTime requestTime) {
        Optional<CacheControl> control = req.getCacheControl();
        return OptionalUtils.exists(control, cc -> {
            int maxStale = cc.getMaxStale();
            if (maxStale > -1) {
                long ttl = item.getTTL();
                long age = item.getAge(requestTime);
                return (ttl - maxStale - age) < 0;
            }
            return true;
        });
    }

    HTTPResponse rewriteStaleResponse(HTTPRequest request, HTTPResponse cachedResponse, long age) {
        return rewriteResponse(request, cachedResponse, true, age, age < 0);
    }

    HTTPResponse rewriteResponse(HTTPRequest request, HTTPResponse cachedResponse, long age) {
        return rewriteResponse(request, cachedResponse, false, age, age < 0);
    }

    private HTTPResponse rewriteResponse(HTTPRequest request, HTTPResponse cachedResponse, boolean stale, long age, boolean hasBeenCached) {
        HTTPResponse response = cachedResponse;
        Headers headers = cachedResponse.getHeaders();
        if (request.getMethod().isSafe()) {
            if (age < 0) {
                headers = headers.add(cacheHeaderBuilder.createMISSXCacheHeader());
            }
            else {
            	if(stale) {
                    headers = headers.add(cacheHeaderBuilder.createHITXCacheHeader());
                    headers = headers.set(HeaderConstants.AGE, String.valueOf(age));
            	} else {
                    if (headers.contains(HeaderConstants.X_CACHE)) {
                        headers = headers.add(cacheHeaderBuilder.createMISSXCacheHeader());
                    } else {
                        headers = headers.add(cacheHeaderBuilder.createHITXCacheHeader());
                        headers = headers.set(HeaderConstants.AGE, String.valueOf(age));
                    }
                }
            }
        }
        if (request.getMethod() == HTTPMethod.GET) {
            List<Tag> noneMatch = request.getHeaders().getConditionals().getNoneMatch();
            Optional<Tag> eTag = response.getHeaders().getETag();
            if (eTag.isPresent() && !noneMatch.isEmpty() && !hasBeenCached) {
                if (noneMatch.contains(eTag.get()) || noneMatch.contains(Tag.ALL)) {
                    response = new HTTPResponse(Status.NOT_MODIFIED, headers);
                }
            }
            Optional<LocalDateTime> lastModified = response.getHeaders().getLastModified();
            Optional<LocalDateTime> modifiedSince = request.getHeaders().getConditionals().getModifiedSince();
            if (lastModified.isPresent() && modifiedSince != null && !hasBeenCached) {
                if (lastModified.equals(modifiedSince)) {
                    response = new HTTPResponse(Status.NOT_MODIFIED, headers);
                }
            }
        }
        else if (request.getMethod() == HTTPMethod.HEAD) {
            response = new HTTPResponse(response.getStatus(), headers);
        }
        if (stale) {
            headers = warnStale(headers);
        }
        return response.withHeaders(headers);
    }


    boolean shouldBeStored(HTTPResponse response) {
        boolean hasValidator = response.getHeaders().getLastModified().isPresent() || response.getHeaders().getETag().isPresent();
        boolean hasExpiry = response.getHeaders().getExpires().isPresent() || (OptionalUtils.exists(response.getHeaders().getCacheControl(), cc -> cc.getMaxAge() > 0));
        return hasExpiry || hasValidator;
    }
}
