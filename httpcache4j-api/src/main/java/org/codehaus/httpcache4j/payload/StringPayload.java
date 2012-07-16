package org.codehaus.httpcache4j.payload;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import org.codehaus.httpcache4j.MIMEType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class StringPayload implements Payload {
    private final String value;
    private final MIMEType mimeType;
    private Charset charset;

    public StringPayload(String value, MIMEType mimeType) {
        this(value, mimeType, Charsets.UTF_8);
    }

    public StringPayload(String value, MIMEType mimeType, Charset charset) {
        this.value = Preconditions.checkNotNull(value, "String value may not be null");
        this.mimeType = Preconditions.checkNotNull(mimeType, "MIMEType may not be null");
        this.charset = charset != null ? charset : Charsets.UTF_8;
    }

    public MIMEType getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(value.getBytes(charset));
    }

    public boolean isAvailable() {
        return true;
    }

    public long length() {
        return value.length();
    }
}
