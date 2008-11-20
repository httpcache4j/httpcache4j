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
import org.apache.commons.lang.Validate;

import org.codehaus.httpcache4j.HTTPException;
import org.codehaus.httpcache4j.MIMEType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.ObjectStreamException;
import java.util.UUID;

/**
 * Stores the file in "bucket".
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class CleanableFilePayload implements CleanablePayload, Serializable {

    private static final long serialVersionUID = -4565379464661253740L;
    
    private final String fileName;
    private transient FileGenerationManager generationManager;
    private final MIMEType mimeType;
    //TODO: is this a good idea? Should the generationManager be serializable instead?
    private final int generations;
    private final int generationSize;
    private final File baseDirectory;

    public CleanableFilePayload(FileGenerationManager generationManager, InputStream stream, MIMEType mimeType) throws IOException {
        Validate.notNull(generationManager, "You may not add a null generation manager");
        Validate.notNull(stream, "You may not add a null stream");
        this.mimeType = mimeType;
        fileName = UUID.randomUUID().toString();
        this.generationManager = generationManager;
        generationSize = generationManager.getGenerationSize();
        generations = generationManager.getNumberOfGenerations();
        baseDirectory = generationManager.getBaseDirectory();
        File file = getFile();
        FileOutputStream outputStream = FileUtils.openOutputStream(file);
        try {
            IOUtils.copy(stream, outputStream);
        }
        finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(stream);
        }
        //TODO: Should we do something if the file's length() is 0 ? Throw Exception? ignore it? delete it?
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
        return null;
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

    //serialization stuff.
    private Object readResolve() throws ObjectStreamException {
        generationManager = new FileGenerationManager(baseDirectory, generations, generationSize);
        return this;
    }
}