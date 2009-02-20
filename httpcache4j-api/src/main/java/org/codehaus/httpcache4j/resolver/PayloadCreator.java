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

import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.payload.Payload;

import java.io.InputStream;
import java.net.URI;

/**
 * Creates a payload from the response. Most users will want to use the
 * DefaultPayloadCreator in the core project.
 * This is expected to be used with the ResponseResolver.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public interface PayloadCreator {
    /**
     * Creates a payload useable by the response.
     *
     * @param requestURI
     *@param responseHeaders the headers to determine cacheablity
     * @param stream  the stream to create the payload from.
 *   @return the created payload
     */
    Payload createPayload(URI requestURI, Headers responseHeaders, InputStream stream);
}
