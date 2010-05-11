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

import java.io.IOException;
import java.net.SocketException;
import java.util.*;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
class HTTPCacheHelper {
    private static final Set<HTTPMethod> safeMethods;
    private static final Set<String> unmodifiableHeaders;
    private static final Set<Status> cacheableStatuses;

    static {
        // ref http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.1
        safeMethods = ImmutableSet.of(
                HTTPMethod.CONNECT,
                HTTPMethod.GET,
                HTTPMethod.HEAD,
                HTTPMethod.OPTIONS,
                HTTPMethod.TRACE
        );

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



    HTTPResponse warn(HTTPResponse response, IOException e) {
        Headers headers = new Headers(response.getHeaders());
        headers = headers.add(Warning.STALE_WARNING.toHeader());
        if (e instanceof SocketException) {
            headers = headers.add(Warning.DISCONNECT_OPERATION_WARNING.toHeader());
        }
        return new HTTPResponse(response.getPayload(), response.getStatus(), headers);
    }


    HTTPResponse calculateAge(final HTTPRequest request, final HTTPResponse response, final CacheItem cacheItem) {
        return new HTTPResponse(response.getPayload(), response.getStatus(), response.getHeaders().add(HeaderConstants.AGE, Integer.toString(cacheItem.getAge(request))));
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
        return HeaderUtils.hasCacheableHeaders(headers);

    }

    boolean isSafeRequest(HTTPRequest request) {
        return safeMethods.contains(request.getMethod());
    }

    HTTPRequest prepareConditionalRequest(HTTPRequest request, HTTPResponse staleResponse) {
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

    public HTTPResponse warnStale(HTTPResponse response) {
        return warn(response, null);
    }
}
