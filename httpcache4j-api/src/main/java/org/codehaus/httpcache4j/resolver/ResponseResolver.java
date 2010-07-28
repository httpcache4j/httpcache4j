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

package org.codehaus.httpcache4j.resolver;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;

import java.io.IOException;

/**
 * The basic interface to resolve a response with the originating server.
 * This is used internally by the HTTPCache, but users may use this directly if they do not require the caching
 * features. Implementors will also want to use the {@link org.codehaus.httpcache4j.resolver.ResponseCreator response creator}, as well.
 * Implementors would want to extend {@link AbstractResponseResolver} instead of using the interface directly.
 *
 * @since 1.0
 */
public interface ResponseResolver {
    /**
     * Resolves the given request into a response.
     *
     * @param request the request to resolve.
     *
     * @return the raw response from the server.
     * @throws java.io.IOException if an IOException occurs e.g: java.net.ConnectException.
     */
    HTTPResponse resolve(HTTPRequest request) throws IOException;

    void shutdown();
}