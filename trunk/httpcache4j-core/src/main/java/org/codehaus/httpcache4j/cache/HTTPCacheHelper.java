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
import static org.codehaus.httpcache4j.HeaderConstants.VARY;
import static org.codehaus.httpcache4j.HeaderConstants.ETAG;
import org.joda.time.Seconds;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.SocketException;
import java.util.*;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
class HTTPCacheHelper {
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



    HTTPResponse warn(HTTPResponse response, IOException e) {
        Headers headers = new Headers(response.getHeaders().getHeadersAsMap());
        headers.add(Warning.STALE_WARNING.toHeader());
        if (e instanceof SocketException) {
            headers.add(Warning.DISCONNECT_OPERATION_WARNING.toHeader());
        }
        return new HTTPResponse(response.getPayload(), response.getStatus(), headers);
    }


    String calculateAge(final HTTPResponse pResolvedResponse, final HTTPResponse pCachedResponse) {
        DateTime resolved = HeaderUtils.fromHttpDate(pResolvedResponse.getHeaders().getFirstHeader(HeaderConstants.DATE));
        DateTime cached = HeaderUtils.fromHttpDate(pCachedResponse.getHeaders().getFirstHeader(HeaderConstants.DATE));
        if (resolved == null || cached == null) {
            return "0";
        }
        return String.valueOf(Seconds.secondsBetween(cached, resolved).getSeconds());
    }

    Headers removeUnmodifiableHeaders(Headers headers) {
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

    Vary determineVariation(Headers responseHeaders, Headers requestHeaders) {
        String varyHeader = responseHeaders.getFirstHeaderValue(VARY);
        Map<String, String> resolvedVaryHeaders = new HashMap<String, String>();
        if (varyHeader != null) {
            String[] varies = varyHeader.split(",");
            for (String vary : varies) {
                String value = requestHeaders.getFirstHeaderValue(vary);
                resolvedVaryHeaders.put(vary, value == null ? null : value);
            }
        }
        return new Vary(resolvedVaryHeaders);
    }

    boolean isCacheableResponse(HTTPResponse response) {
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

    boolean isSafeRequest(HTTPRequest request) {
        return safeMethods.contains(request.getMethod());
    }

    boolean isCacheableRequest(HTTPRequest request) {
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

    void prepareConditionalRequest(HTTPRequest request, HTTPResponse staleResponse) {
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

}
