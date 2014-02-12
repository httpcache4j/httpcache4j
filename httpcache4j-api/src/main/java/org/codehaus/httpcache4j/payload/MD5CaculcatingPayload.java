package org.codehaus.httpcache4j.payload;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;

import com.google.common.io.ByteSource;
import com.google.common.io.FileBackedOutputStream;
import org.codehaus.httpcache4j.HTTPException;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.util.Digester;
import org.codehaus.httpcache4j.util.Hex;
import org.codehaus.httpcache4j.util.IOUtils;

/**
 * Calculates the MD5 of the payload.
 * Copies the payload into a byte array if the data is small enough, changes to a backing file if the
 * content is large.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class MD5CaculcatingPayload implements Payload {
    private final MIMEType mimeType;
    private final long length;
    private final String md5;
    private final ByteSource byteSource;

    private MD5CaculcatingPayload(final InputStream stream, MIMEType mimeType, long length) {
        this.mimeType = mimeType;
        this.length = length;
        FileBackedOutputStream os = new FileBackedOutputStream(1024);
        try {
            DigestInputStream md5Stream = new DigestInputStream(stream, Digester.getDigest("MD5"));
            IOUtils.copy(md5Stream, os);
            this.md5 = Hex.encode(md5Stream.getMessageDigest().digest());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byteSource = os.asByteSource();
    }

    public String getMD5() {
        return md5;
    }

    public MIMEType getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        try {
            return byteSource.openStream();
        } catch (IOException e) {
            throw new HTTPException(e);
        }
    }

    public long length() {
        return length;
    }

    public boolean isAvailable() {
        return true;
    }
    
    public static MD5CaculcatingPayload payloadFor(Payload p) {
        return new MD5CaculcatingPayload(p.getInputStream(), p.getMimeType(), p.length());
    }
}
