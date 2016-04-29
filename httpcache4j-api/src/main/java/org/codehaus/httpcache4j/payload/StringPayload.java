package org.codehaus.httpcache4j.payload;

import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.util.IOUtils;

import java.io.IOException;
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

    public static StringPayload fromInputStream(InputStream is, MIMEType mime, Charset charset) throws IOException {
        try(InputStream i = is) {
            return new StringPayload(new String(IOUtils.toByteArray(i), charset), mime, charset);
        }
    }

    public static StringPayload fromInputStream(InputStream is, MIMEType mime) throws IOException {
        return fromInputStream(is, mime, StandardCharsets.UTF_8);
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
