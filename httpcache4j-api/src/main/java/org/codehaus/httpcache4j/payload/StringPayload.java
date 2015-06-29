package org.codehaus.httpcache4j.payload;

import org.codehaus.httpcache4j.MIMEType;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public final class StringPayload implements Payload, Serializable {
    private final ByteArrayPayload delegate;
    private final String value;
    private final Charset charset;

    public StringPayload(String value, MIMEType mimeType) {
        this(value, mimeType, StandardCharsets.UTF_8);
    }

    public StringPayload(String value, MIMEType mimeType, Charset charset) {
        this.delegate = new ByteArrayPayload(value.getBytes(charset), mimeType);
        this.value = value;
        this.charset = charset;
    }

    public String getValue() {
        return value;
    }

    public Charset getCharset() {
        return charset;
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
