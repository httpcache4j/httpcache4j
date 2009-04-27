package org.codehaus.httpcache4j.resolver;

import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.codehaus.httpcache4j.*;

import java.net.URI;
import java.io.InputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public abstract class AbstractPayloadCreator implements PayloadCreator {
    
    public final Payload createPayload(URI requestURI, Headers responseHeaders, InputStream stream) {
        boolean cacheable = HeaderUtils.hasCacheableHeaders(responseHeaders);
        Header contentTypeHeader = responseHeaders.getFirstHeader(HeaderConstants.CONTENT_TYPE);
        MIMEType type = contentTypeHeader != null ? MIMEType.valueOf(contentTypeHeader.getValue()) : MIMEType.APPLICATION_OCTET_STREAM;
        if (cacheable) {
            try {
                return createCachedPayload(requestURI, stream, type);
            } catch (IOException e) {
                throw new HTTPException("Unable to create reponse storage", e);
            }
        }
        else {
            return new InputStreamPayload(stream, type);
        }        
    }

    protected abstract Payload createCachedPayload(URI requestURI, InputStream stream, MIMEType type) throws IOException;
}
