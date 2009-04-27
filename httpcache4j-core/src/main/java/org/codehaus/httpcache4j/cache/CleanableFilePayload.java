/*
 * Copyright (c) 2008, The Codehaus. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.codehaus.httpcache4j.cache;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ClosedInputStream;
import org.apache.commons.lang.Validate;

import org.codehaus.httpcache4j.HTTPException;
import org.codehaus.httpcache4j.MIMEType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.UUID;

/**
 * Stores the file in "bucket". This class is responsible creating a
 * file in the file generation manager so we can copy the remote stream
 * into it.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
//TODO: file locks. We could solve this by introducing some read and write lock.
//todo: there may be many read locks, but only one write lock.
//TODO: probably needs refactoring of this class.
public class CleanableFilePayload implements CleanablePayload, Serializable {

    private static final long serialVersionUID = -4565379464661253740L;
    
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
        if (file.length() == 0) {
            file.delete();
        }
    }

    private File getFile() {
        return generationManager.getFile(fileName);
    }

    public MIMEType getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        File file = getFile();
        if (isAvailable(file)) {
            try {
                return FileUtils.openInputStream(file);
            }
            catch (IOException e) {
                throw new HTTPException("Could not create file input stream", e);
            }
        }
        return new ClosedInputStream();
    }

    public boolean isTransient() {
        return false;
    }

    public void clean() {
        final File file = getFile();
        if (file != null && isAvailable(file)) {
            file.delete();
        }
    }

    public boolean isAvailable() {
        File file = getFile();
        return isAvailable(file);
    }

    private boolean isAvailable(File file) {
        return file.exists() && file.canRead() && file.canWrite();
    }
}