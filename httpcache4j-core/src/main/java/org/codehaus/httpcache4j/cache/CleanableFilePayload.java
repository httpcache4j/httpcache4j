package org.codehaus.httpcache4j.cache;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;

import org.codehaus.httpcache4j.HTTPException;
import org.codehaus.httpcache4j.MIMEType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Stores the file in "bucket".
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class CleanableFilePayload implements CleanablePayload {

    private final String fileName;
    private final FileGenerationManager generationManager;
    private final MIMEType mimeType;

    public CleanableFilePayload(FileGenerationManager generationManager, InputStream stream, MIMEType mimeType) throws IOException {
        Validate.notNull(generationManager, "You may not add a null generation manager");
        Validate.notNull(stream, "You may not add a null stream");
        this.mimeType = mimeType;
        fileName = UUID.randomUUID().toString();
        this.generationManager = generationManager;
        File file = getFile();
        FileOutputStream outputStream = FileUtils.openOutputStream(file);
        try {
            IOUtils.copy(stream, outputStream);
        }
        finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(stream);
        }
    }

    private File getFile() {
        return generationManager.getFile(fileName);
    }

    public MIMEType getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        if (isAvailable()) {
            File file = getFile();
            try {
                return FileUtils.openInputStream(file);
            }
            catch (IOException e) {
                throw new HTTPException("Could not create file input stream", e);
            }
        }
        return null;
    }

    public boolean isTransient() {
        return false;
    }

    public void clean() {
        generationManager.removeFile(fileName);
    }

    public boolean isAvailable() {
        File file = getFile();
        return file.exists() && file.canRead() && file.canWrite();
    }
}