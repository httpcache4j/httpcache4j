package org.codehaus.httpcache4j.cache;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.httpcache4j.HTTPException;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.payload.FilePayload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class CleanableFilePayload extends FilePayload implements CleanablePayload {

    public CleanableFilePayload(File fileStorageDirectory, InputStream stream, MIMEType mimeType) throws IOException {
        super(new File(fileStorageDirectory, UUID.randomUUID().toString()), mimeType);
        FileOutputStream outputStream = FileUtils.openOutputStream(file);
        try {
            IOUtils.copy(stream, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(stream);
        }

    }

    public void clean() {
        if (isAvailable()) {
            if (!file.delete()) {
                throw new HTTPException("No file available for this response...");
            }
        }
    }

    public boolean isAvailable() {
        return super.isAvailable() && file.canWrite();
    }
}