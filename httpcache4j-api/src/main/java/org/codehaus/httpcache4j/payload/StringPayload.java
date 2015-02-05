package org.codehaus.httpcache4j.payload;

import com.google.common.base.Charsets;
import org.codehaus.httpcache4j.MIMEType;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public final class StringPayload implements Payload {
    private final ByteArrayPayload delegate;

    public StringPayload(String value, MIMEType mimeType) {
        this(value, mimeType, Charsets.UTF_8);
    }

    public StringPayload(String value, MIMEType mimeType, Charset charset) {
        delegate = new ByteArrayPayload(value.getBytes(charset), mimeType);
    }

    @Override
    public MIMEType getMimeType() {
        return delegate.getMimeType();
    }

    @Override
    public InputStream getInputStream() {
        return delegate.getInputStream();
    }

    @Override
    public long length() {
        return delegate.length();
    }

    @Override
    public boolean isAvailable() {
        return delegate.isAvailable();
    }
}
