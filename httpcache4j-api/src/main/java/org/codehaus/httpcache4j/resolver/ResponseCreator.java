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

package org.codehaus.httpcache4j.resolver;

import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.codehaus.httpcache4j.util.NumberUtils;

import java.io.InputStream;
import java.util.Optional;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public final class ResponseCreator {

    public static HTTPResponse createResponse(final StatusLine line, final Headers responseHeaders, final Optional<InputStream> stream) {
        Optional<String> contentLengthHeader = responseHeaders.getFirstHeaderValue(HeaderConstants.CONTENT_LENGTH);

        MIMEType type = responseHeaders.getContentType().orElse(MIMEType.APPLICATION_OCTET_STREAM);
        Optional<Long> length = responseHeaders.getContentLength();

        Optional<Payload> payload = stream.
                filter(is -> line.getStatus().isBodyContentAllowed()).
                map(is -> new InputStreamPayload(is, type, length.orElse(-1L)));

        return new HTTPResponse(payload, line, responseHeaders);
    }    
}
