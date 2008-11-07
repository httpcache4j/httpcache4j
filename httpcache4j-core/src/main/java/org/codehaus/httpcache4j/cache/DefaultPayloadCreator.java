package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.resolver.PayloadCreator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public class DefaultPayloadCreator implements PayloadCreator {
    private FileGenerationManager fileGenerationManager;

    public DefaultPayloadCreator(String fileStorageDirectory) {
        fileGenerationManager = new FileGenerationManager(new File(fileStorageDirectory), 10, 100);
    }

    public Payload createPayload(Headers headers, InputStream stream) {
        boolean cacheable = HTTPUtils.hasCacheableHeaders(headers);
        Header contentTypeHeader = headers.getFirstHeader(HeaderConstants.CONTENT_TYPE);
        if (cacheable) {
            try {
                return new CleanableFilePayload(fileGenerationManager, stream, new MIMEType(contentTypeHeader.getValue()));
            }
            catch (IOException e) {
                throw new HTTPException("Unable to create reponse storage", e);
            }
        }
        else {
            return new InputStreamPayload(stream, new MIMEType(contentTypeHeader.getValue()));
        }
    }

}
