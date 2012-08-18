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

import com.google.common.collect.ImmutableSet;
import org.codehaus.httpcache4j.*;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.SocketException;
import java.util.*;

import static org.codehaus.httpcache4j.HeaderConstants.CACHE_CONTROL;

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

        /**
         * 200, 203, 206, 300, 301 or 410
         */
        cacheableStatuses = ImmutableSet.of(Status.OK,
                                       Status.NON_AUTHORITATIVE_INFORMATION,
                                       Status.MULTIPLE_CHOICES,
                                       Status.MOVED_PERMANENTLY,
                                       Status.GONE
        );
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
        Set<String> usableHeaders = new HashSet<String>(headers.keySet());
        usableHeaders.removeAll(unmodifiableHeaders);
        for (String headerName : usableHeaders) {
            if (headers.hasHeader(headerName)) {
                washedHeaders = washedHeaders.add(headerName, headers.getHeaders((headerName)));
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
        Conditionals conditionals = request.getConditionals();
        if (request.getMethod() == HTTPMethod.GET && conditionals.toHeaders().isEmpty()) {
            if (staleResponse.getETag() != null) {
                conditionals = new Conditionals().addIfNoneMatch(staleResponse.getETag());
            }
            else if (staleResponse.getLastModified() != null) {
                conditionals = conditionals.ifModifiedSince(staleResponse.getLastModified());
            }
            return request.conditionals(conditionals);
        }
        return request;
    }

    Headers warnStale(Headers headers) {
        return warn(headers, null);
    }

    boolean isCacheableRequest(HTTPRequest request) {
        if (request.getMethod().isCacheable()) {
            if (request.getHeaders().hasHeader(CACHE_CONTROL)) {
                CacheControl cc = request.getCacheControl();
                //If the request tells us that we shouldn't cache the response, then we don't.
                return !cc.isNoCache() || !cc.isNoStore();
            }
            return true;
        }
        return false;
    }

    boolean allowStale(CacheItem item, HTTPRequest req) {
        if (req.getHeaders().hasHeader(CACHE_CONTROL)) {
            CacheControl control = req.getCacheControl();
            int maxStale = control.getMaxStale();
            if (maxStale > -1) {
                int ttl = item.getTTL();
                int age = item.getAge(req);
                if ((ttl - maxStale - age) < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    HTTPResponse rewriteStaleResponse(HTTPRequest request, HTTPResponse cachedResponse, int age) {
        return rewriteResponse(request, cachedResponse, true, age);
    }

    HTTPResponse rewriteResponse(HTTPRequest request, HTTPResponse cachedResponse, int age) {
        return rewriteResponse(request, cachedResponse, false, age);
    }

    private HTTPResponse rewriteResponse(HTTPRequest request, HTTPResponse cachedResponse, boolean stale, int age) {
        HTTPResponse response = cachedResponse;
        Headers headers = response.getHeaders();
        if (request.getMethod().isSafe()) {
            if (age < 0) {
                headers = headers.add(cacheHeaderBuilder.createMISSXCacheHeader());
            }
            else {
            	if(stale) {
                    headers = headers.add(cacheHeaderBuilder.createHITXCacheHeader());
                    headers = headers.set(HeaderConstants.AGE, String.valueOf(age));
            	} else {
                    if (headers.hasHeader(HeaderConstants.X_CACHE)) {
                        headers = headers.add(cacheHeaderBuilder.createMISSXCacheHeader());
                    } else {
                        headers = headers.add(cacheHeaderBuilder.createHITXCacheHeader());
                        headers = headers.set(HeaderConstants.AGE, String.valueOf(age));
                    }
                }
            }            
        }
        if (request.getMethod() == HTTPMethod.GET) {
            List<Tag> noneMatch = request.getConditionals().getNoneMatch();
            Tag eTag = response.getETag();
            if (eTag != null && !noneMatch.isEmpty()) {
                if (noneMatch.contains(eTag) || noneMatch.contains(Tag.ALL)) {
                    response = new HTTPResponse(null, Status.NOT_MODIFIED, headers);
                }
            }
            DateTime lastModified = response.getLastModified();
            DateTime modifiedSince = request.getConditionals().getModifiedSince();
            if (lastModified != null && modifiedSince != null) {
                if (lastModified.equals(modifiedSince)) {
                    response = new HTTPResponse(null, Status.NOT_MODIFIED, headers);
                }
            }
        }
        else if (request.getMethod() == HTTPMethod.HEAD) {
            response = new HTTPResponse(null, response.getStatus(), headers);
        }
        if (stale) {
            headers = warnStale(headers);
        }
        return response.withHeaders(headers);
    }


    boolean shouldBeStored(HTTPResponse response) {
        boolean hasValidator = response.getLastModified() != null || response.getETag() != null;
        boolean hasExpiry = response.getExpires() != null || (response.getCacheControl() != null && response.getCacheControl().getMaxAge() > 0);
        return hasExpiry || hasValidator;
    }
}
