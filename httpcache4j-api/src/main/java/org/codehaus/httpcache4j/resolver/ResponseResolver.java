package org.codehaus.httpcache4j.resolver;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;

/**
 * The basic interface to resolve a response with the originating server.
 * This is used internally by the HTTPCache, but users may use this directly if they do not require the caching
 * features. Implementors will also want to use the {@link PayloadCreator payload creator}, as well.
 * Implementors would want to extend {@link AbstractResponseResolver} instead of using the interface directly.
 *
 * @since 1.0
 */
public interface ResponseResolver {
    /**
     * Resolves the given request into a response.
     * 
     * @param request the request to resolve.
     * @return the raw response from the server.
     */
    HTTPResponse resolve(HTTPRequest request);
}