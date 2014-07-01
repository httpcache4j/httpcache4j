package org.codehaus.httpcache4j.payload;

import java.io.*;
import java.security.DigestInputStream;
import java.util.UUID;

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
    private final File backupFile;

    private MD5CaculcatingPayload(final InputStream stream, MIMEType mimeType, long length) {
        this.mimeType = mimeType;
        this.length = length;
        try {
            backupFile = File.createTempFile(UUID.randomUUID().toString(), "bak");
            FileOutputStream os = new FileOutputStream(backupFile);
            DigestInputStream md5Stream = new DigestInputStream(stream, Digester.getDigest("MD5"));
            try {
                IOUtils.copy(md5Stream, os);
                this.md5 = Hex.encode(md5Stream.getMessageDigest().digest());
            }
            finally {
                IOUtils.closeQuietly(md5Stream);
                IOUtils.closeQuietly(os);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getMD5() {
        return md5;
    }

    public MIMEType getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        try {
            return new FileInputStream(backupFile);
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
