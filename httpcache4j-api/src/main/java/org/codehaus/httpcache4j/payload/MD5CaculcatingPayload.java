package org.codehaus.httpcache4j.payload;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.util.AvailableInputStream;

/**
 * Requires that the payload can be re-used. If the delegate is an InputstreamPayload, the stream will be changed into a byte-array.
 * This WILL be a problem with large entities, as the entire content must reside in memory.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class MD5CaculcatingPayload implements Payload {
    private final MIMEType mimeType;
    private final long length;
    private final AvailableInputStream stream;
    private final String md5;

    private MD5CaculcatingPayload(final InputStream stream, MIMEType mimeType, long length) {
        this.mimeType = mimeType;
        this.length = length;
        try {
            stream.mark(Integer.MAX_VALUE);
            this.md5 = hash(stream).toString();
            stream.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.stream = new AvailableInputStream(stream);
    }

    public String getMD5() {
        return md5;
    }

    public MIMEType getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        return stream;
    }

    public long length() {
        return length;
    }

    public boolean isAvailable() {
        return stream.isAvailable();
    }
    
    public static MD5CaculcatingPayload payloadFor(Payload p) {
        InputStream stream = p.getInputStream();
        if (!stream.markSupported()) {
            stream = new BufferedInputStream(stream, 8048);
        }
        return new MD5CaculcatingPayload(stream, p.getMimeType(), p.length());
    }

    private HashCode hash(final InputStream stream) throws IOException {
        return ByteStreams.hash(new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                return stream;
            }
        }, Hashing.md5());
    }
}
