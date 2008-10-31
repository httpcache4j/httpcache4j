package org.httpcache4j.cache;

import org.httpcache4j.*;
import org.httpcache4j.payload.InputStreamPayload;
import org.httpcache4j.payload.Payload;
import org.httpcache4j.resolver.PayloadCreator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class DefaultPayloadCreator implements PayloadCreator {
    private File fileStorageDirectory;

    public DefaultPayloadCreator(String fileStorageDirectory) {
        this.fileStorageDirectory = new File(fileStorageDirectory);
    }

    public Payload createPayload(Headers headers, InputStream stream) {
        boolean cacheable = HTTPUtils.hasCacheableHeaders(headers);
        Header contentTypeHeader = headers.getFirstHeader(HeaderConstants.CONTENT_TYPE);
        if (cacheable) {
            try {
                return new CleanableFilePayload(fileStorageDirectory, stream, new MIMEType(contentTypeHeader.getValue()));
            } catch (IOException e) {
                throw new HTTPException("Unable to create reponse storage", e);
            }
        } else {
            return new InputStreamPayload(stream, new MIMEType(contentTypeHeader.getValue()));
        }
    }

}
