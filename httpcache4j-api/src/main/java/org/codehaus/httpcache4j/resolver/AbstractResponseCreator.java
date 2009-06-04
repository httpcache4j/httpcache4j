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
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.payload.ClosedInputStreamPayload;
import org.apache.commons.lang.Validate;

import java.io.InputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public abstract class AbstractResponseCreator implements ResponseCreator {
    private final StoragePolicy storagePolicy;

    protected AbstractResponseCreator(StoragePolicy storagePolicy) {
        Validate.notNull("Storage policy may not be null, use other constructor if you do not have a storage policy");
        this.storagePolicy = storagePolicy;
    }

    protected AbstractResponseCreator() {
        this(StoragePolicy.NULL);
    }

    public synchronized HTTPResponse createResponse(final HTTPRequest request, final Status status, final Headers responseHeaders, final InputStream stream) {
        boolean cacheable = HeaderUtils.hasCacheableHeaders(responseHeaders) && HTTPUtil.isCacheableRequest(request);
        Header contentTypeHeader = responseHeaders.getFirstHeader(HeaderConstants.CONTENT_TYPE);
        MIMEType type = contentTypeHeader != null ? MIMEType.valueOf(contentTypeHeader.getValue()) : MIMEType.APPLICATION_OCTET_STREAM;
        Payload payload = null;
        if (status.isBodyContentAllowed()) {
            if (cacheable && stream != null) {
                try {
                    payload = createCachedPayload(request, responseHeaders, stream, type);
                } catch (IOException e) {
                    throw new HTTPException("Unable to create payload for response", e);
                }
            }
            else if (stream != null)  {
                payload = new InputStreamPayload(stream, type);
            }
        }
        return new HTTPResponse(payload, status, responseHeaders);
    }

    protected StoragePolicy getStoragePolicy() {
        return storagePolicy;
    }

    protected abstract Payload createCachedPayload(HTTPRequest request, Headers responseHeaders, InputStream stream, MIMEType type) throws IOException;
}
