package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.*;
import static org.codehaus.httpcache4j.HeaderConstants.ETAG;
import static org.codehaus.httpcache4j.HeaderConstants.VARY;
import org.codehaus.httpcache4j.resolver.ResponseResolver;

import org.apache.commons.lang.Validate;

import java.util.*;

/**
 * TODO:
 * Support Warning header http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46
 * Support detecting empty responses? http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.4
 * Support Range headers. http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35
 *
 */


/**
 * The main HTTPCache class.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * */
public class HTTPCache {
    /** Directive to the {@code "Cache-Control"} header */
    public static final String HEADER_DIRECTIVE_MAX_AGE = "max-age";

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

    private final CacheStorage storage;
    private ResponseResolver resolver;

    public HTTPCache(CacheStorage storage, ResponseResolver resolver) {
        Validate.notNull(storage, "Cache storage may not be null");
        Validate.notNull(resolver, "Resolver may not be null");
        this.storage = storage;
        this.resolver = resolver;
    }

    public HTTPCache(CacheStorage storage) {
        Validate.notNull(storage, "Cache storage may not be null");
        this.storage = storage;
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

    public HTTPResponse doCachedRequest(HTTPRequest request) {
        return doCachedRequest(request, false);
    }

    public HTTPResponse doCachedRequest(HTTPRequest request, boolean force) {
        HTTPResponse response;
        if (!isCacheableRequest(request)) {
            if (!isSafeRequest(request)) {
                storage.invalidate(request.getRequestURI());
            }
            response = resolver.resolve(request);
        }
        else {
            //request is cacheable
            response = getFromCache(request, force);
        }
        return response;
    }

    private HTTPResponse getFromCache(HTTPRequest request, final boolean force) {
        HTTPResponse response;
        if (!force) {
            CacheItem item = storage.get(request);
            if (item != null && item.isStale()) {
                //If the cached value is stale, execute the request and try to cache it.
                HTTPResponse staleResponse = item.getResponse();
                if (request.getConditionals().getNonMatch().isEmpty() && staleResponse.getHeaders().hasHeader(ETAG)) {
                    addIfNoneMatchHeader(staleResponse.getHeaders().getFirstHeader(ETAG), request);
                }

                response = handleResolve(request, item);
            }
            else if (item != null) {
                response = item.getResponse();
            }
            else {
                response = handleResolve(request, null);
            }
        }
        else {
            response = handleResolve(request, null);
        }

        return response;
    }

    private HTTPResponse handleResolve(HTTPRequest request, CacheItem item) {
        HTTPResponse response;
        HTTPResponse resolvedResponse = resolver.resolve(request);

        try {
            if (isCacheableResponse(resolvedResponse)) {
                Vary vary = determineVariation(resolvedResponse, request);

                storage.put(request.getRequestURI(), vary, new CacheItem(resolvedResponse));
                response = resolvedResponse;
            }
            else if (item != null && resolvedResponse.getStatus().getCode() == Status.NOT_MODIFIED.getCode()) {
                //replace the cached entry as the entry was not modified
                HTTPResponse cachedResponse = item.getResponse();
                HTTPResponse updatedResponse;
                LinkedHashMap<String, List<Header>> headers = new LinkedHashMap<String, List<Header>>(cachedResponse.getHeaders().getHeadersAsMap());

                headers.putAll(removeUnmodifiableHeaders(resolvedResponse.getHeaders()).getHeadersAsMap());
                Headers realHeaders = new Headers(headers);
                updatedResponse = new HTTPResponse(cachedResponse.getPayload(), resolvedResponse.getStatus(), realHeaders);
                Vary vary = determineVariation(updatedResponse, request);
                storage.put(request.getRequestURI(), vary, new CacheItem(updatedResponse));
                response = updatedResponse;
            }
            else {
                //Response was not cacheable
                response = resolvedResponse;
            }
            if (item != null && resolvedResponse.getStatus().getCode() == Status.OK.getCode()) {
                //Success was ok, but we had already a response for this item.
                //invalidate it so we don't clutter the filesystem.
                storage.invalidate(request.getRequestURI(), item);
            }
        }
        catch (HTTPException e) {
            if (item != null) {
                storage.invalidate(request.getRequestURI(), item);
            }
            response = resolvedResponse;
        }
        return response;
    }

    private Headers removeUnmodifiableHeaders(Headers headers) {
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

    private Vary determineVariation(HTTPResponse resolvedResponse, HTTPRequest request) {
        Header varyHeader = resolvedResponse.getHeaders().getFirstHeader(VARY);
        Map<String, String> resolvedVaryHeaders = new HashMap<String, String>();
        if (varyHeader != null) {
            String[] varies = varyHeader.getValue().split(",");
            for (String vary : varies) {
                Header value = request.getHeaders().getFirstHeader(vary);
                resolvedVaryHeaders.put(vary, value == null ? null : value.getValue());
            }
        }
        return new Vary(resolvedVaryHeaders);
    }

    private boolean isCacheableResponse(HTTPResponse response) {
        if (response.getStatus().getCode() != Status.OK.getCode()) {
            return false;
        }
        Headers headers = response.getHeaders();
        return HTTPUtils.hasCacheableHeaders(headers);

    }

    private void addIfNoneMatchHeader(final Header eTagHeader, HTTPRequest request) {
        Tag tag = eTagHeader == null ? null : Tag.parse(eTagHeader.getValue());
        if (tag != null && tag != Tag.ALL) {
            request.getConditionals().addIfNoneMatch(tag);
        }
    }

    private boolean isSafeRequest(HTTPRequest request) {
        return safeMethods.contains(request.getMethod());
    }

    private boolean isCacheableRequest(HTTPRequest request) {
        if (request.getMethod() == HTTPMethod.GET || request.getMethod() == HTTPMethod.HEAD) {
            if (request.getHeaders().hasHeader(HeaderConstants.CACHE_CONTROL)) {
                String cacheControlHeaderValue = request.getHeaders().getFirstHeader(HeaderConstants.CACHE_CONTROL).getValue();
                //If the request tells us that we shouldn't cache the response, then we don't.
                return !(cacheControlHeaderValue.contains("no-store")) || cacheControlHeaderValue.contains("no-cache");
            }
            return true;
        }
        return false;
    }
}