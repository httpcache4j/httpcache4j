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

package org.codehaus.httpcache4j.payload;

import com.google.common.base.Preconditions;
import org.codehaus.httpcache4j.HTTPException;
import org.codehaus.httpcache4j.MIMEType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Payload that accepts a file with mimetype.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class FilePayload implements Payload {
    private final File file;
    private final MIMEType mimeType;

    /**
     * Constructs a File payload
     *
     * @param file the file to use, may not be {@code null}.
     * @param mimeType the mime type of the file, may not be {@code null}. 
     */
    public FilePayload(final File file, final MIMEType mimeType) {
        this.file = Preconditions.checkNotNull(file, "File may not be null");
        this.mimeType = Preconditions.checkNotNull(mimeType, "Mime type may not be null");
    }

    public MIMEType getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        if (isAvailable()) {
            try {
                return new FileInputStream(file);
            }
            catch (FileNotFoundException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        throw new HTTPException(String.format("File %s cannot be read.", file.getAbsolutePath()));
    }

    public boolean isAvailable() {
        return file.exists() && file.canRead();
    }

    public long length() {
        return file.length();
    }

    public File getFile() {
        return file;
    }
}
