package org.codehaus.httpcache4j.cache;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.httpcache4j.HTTPException;
import org.codehaus.httpcache4j.MIMEType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class CleanableFilePayload implements CleanablePayload {
    private File file;
    private MIMEType mimeType;

    public CleanableFilePayload(File fileStorageDirectory, InputStream stream, MIMEType mimeType) throws IOException {
        this.mimeType = mimeType;
        file = new File(fileStorageDirectory, UUID.randomUUID().toString());
        FileOutputStream outputStream = FileUtils.openOutputStream(file);
        try {
            IOUtils.copy(stream, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(stream);
        }

    }

    public MIMEType getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        if (isAvailable()) {
            try {
                return FileUtils.openInputStream(file);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    public void clean() {
        if (isAvailable()) {
            if (!file.delete()) {
                throw new HTTPException("No file available for this response...");
            }
        }
    }

    public boolean isAvailable() {
        return file.exists() && file.canRead() && file.canWrite();
    }

    public boolean isTransient() {
        return false;
    }
}