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
import org.codehaus.httpcache4j.util.CacheStatisticsMXBean;
import org.codehaus.httpcache4j.resolver.ResponseResolver;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;

import javax.management.*;
import java.util.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;

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
    /**
     * Directive to the {@code "Cache-Control"} header
     */
    public static final String HEADER_DIRECTIVE_MAX_AGE = "max-age";

    private final HTTPCacheHelper helper = new HTTPCacheHelper();
    private final CacheStatistics statistics = new CacheStatistics();
    private final CacheStorage storage;
    private ResponseResolver resolver;

    public HTTPCache(CacheStorage storage, ResponseResolver resolver) {
        Validate.notNull(storage, "Cache storage may not be null");
        this.storage = storage;
        this.resolver = resolver;
      handleMbeanRegistry();
    }

  public HTTPCache(CacheStorage storage) {
        this(storage, null);
    }

  private void handleMbeanRegistry() {
    try {
      final ObjectName objectname = ObjectName.getInstance("org.codehaus.httpcache4j.cache:type=statistics");
      ManagementFactory.getPlatformMBeanServer().registerMBean(statistics, objectname);
      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        public void run() {
          try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectname);
          }
          catch (Exception ignored) {
          }
        }
      }));
    }
    catch (Exception ignored) {
      //throw new HTTPException("Unable to register statistics Mbean", e);
    }
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

    public HTTPResponse doCachedRequest(final HTTPRequest request) {
        return doCachedRequest(request, false);
    }

    public HTTPResponse doCachedRequest(final HTTPRequest request, boolean force) {
        if (resolver == null) {
            throw new IllegalStateException("The resolver was not set, no point of continuing with the request");
        }
        HTTPResponse response;
        if (!HTTPUtil.isCacheableRequest(request)) {
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

    private HTTPResponse getFromCache(final HTTPRequest request, final boolean force) {
        HTTPResponse response;
        if (force || request.getConditionals().isUnconditional()) {
            response = unconditionalResolve(request);
        }
        else {
            CacheItem item = storage.get(request);
            HTTPRequest req = request;
            if (item != null) {
                statistics.hit();
                if (item.isStale(request.getRequestTime())) {
                    //If the cached value is stale, execute the request and try to cache it.
                    HTTPResponse staleResponse = item.getResponse();
                    //If the payload has been deleted for some reason, we want to do a unconditional GET
                    if (!staleResponse.hasPayload() || staleResponse.getPayload().isAvailable()) {
                        req = helper.prepareConditionalRequest(request, staleResponse);
                    }
                    else {
                        req = request.conditionals(new Conditionals());
                    }

                    response = handleResolve(req, item);
                }
                else {
                    //TODO: handle rewrite of Status... HEAD should probably always return 200 OK.
                    response = rewriteResponse(req, item);
                }
            }
            else {
                statistics.miss();
                response = unconditionalResolve(request);
            }
        }
        return response;
    }

    private HTTPResponse rewriteResponse(HTTPRequest request, CacheItem item) {
        HTTPResponse response = item.getResponse();
        if (request.getMethod() == HTTPMethod.GET) {
            List<Tag> noneMatch = request.getConditionals().getNoneMatch();
            Tag eTag = response.getETag();
            if (eTag != null && !noneMatch.isEmpty()) {
                if (noneMatch.contains(eTag) || noneMatch.contains(Tag.ALL)) {
                    response = new HTTPResponse(null, Status.NOT_MODIFIED, response.getHeaders());
                }
            }
            DateTime lastModified = response.getLastModified();
            DateTime modifiedSince = request.getConditionals().getModifiedSince();
            if (lastModified != null && modifiedSince != null) {
                if (lastModified.equals(modifiedSince)) {
                    response = new HTTPResponse(null, Status.NOT_MODIFIED, response.getHeaders());
                }
            }
        }
        return helper.calculateAge(request, response, item.getCachedTime());
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
            }
        }
        return response;
    }

    private HTTPResponse updateHeadersFromResolved(final HTTPRequest request, final CacheItem item, final HTTPResponse resolvedResponse) {
        HTTPResponse cachedResponse = item.getResponse();
        Headers headers = new Headers(resolvedResponse.getHeaders());
        headers = headers.add(helper.removeUnmodifiableHeaders(resolvedResponse.getHeaders()));
        HTTPResponse updatedResponse = new HTTPResponse(cachedResponse.getPayload(), cachedResponse.getStatus(), headers);

        storage.update(request, updatedResponse);
        return updatedResponse;
    }
}