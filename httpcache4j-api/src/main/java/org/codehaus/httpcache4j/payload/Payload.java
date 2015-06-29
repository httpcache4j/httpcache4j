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

package org.codehaus.httpcache4j.payload;

import org.codehaus.httpcache4j.HTTPException;
import org.codehaus.httpcache4j.MIMEType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a HTTP payload. may be either a {@link org.codehaus.httpcache4j.HTTPRequest request} payload
 * or a {@link org.codehaus.httpcache4j.HTTPResponse response} payload.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public interface Payload {

    /**
     * Return the mime-type of the payload.
     *
     * @return the mime-type
     */
    public MIMEType getMimeType();

    /**
     * Returns the input stream of the payload. This stream MUST be closed when you are done with it.
     *
     * @return the inputstream of the payload, may return {@code null} if the payload is not available.
     *
     */
    public InputStream getInputStream();


    /**
     * Returns the length of the stream. If the payload is used in a {@link org.codehaus.httpcache4j.HTTPResponse},
     * this is the value of the Content-Length header. If the payload is used in a request, it can be set,
     * or calculated based on the type.
     *
     * @return -1 if the length cannot be calculated or set. Otherwise the expected length of the stream.
     */
    public long length();

    /**
     * Returns {@code true} if the payload is available, IE. If the stream can be read from.
     *
     * @return {@code true} if the payload is available. {@code false} if not.
     */
    public boolean isAvailable();

    public default <A> Optional<A> transform(final Function<InputStream, A> f) {
        try(InputStream is = getInputStream()) {
            return Optional.ofNullable(f.apply(is));
        } catch (IOException e) {
            throw new HTTPException(e);
        }
    }
}
