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

import org.codehaus.httpcache4j.HTTPException;
import org.codehaus.httpcache4j.MIMEType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Stores the file in "bucket". This class is responsible creating a
 * file in the file generation manager so we can copy the remote stream
 * into it.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
//TODO: file locks. We could solve this by introducing some read and write lock.
//todo: there may be many read locks, but only one write lock.
//TODO: probably needs refactoring of this class.
public class CleanableFilePayload implements CleanablePayload, Serializable {

    private static final long serialVersionUID = -4565379464661253740L;

    private final File file;
    private final MIMEType mimeType;

    public CleanableFilePayload(final File file, final MIMEType mimeType) {
        this.file = file;
        this.mimeType = mimeType;
    }

    File getFile() {
        return file;
    }

    public MIMEType getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        if (isAvailable()) {
            try {
                return FileUtils.openInputStream(file);
            }
            catch (IOException e) {
                throw new HTTPException("Could not create file input stream", e);
            }
        }
        throw new HTTPException(String.format("File '%s' is gone...", file.getAbsolutePath()));
    }

    public void clean() {
        if (isAvailable()) {
            file.delete();
            File parent = file.getParentFile();
            if (parent != null && parent.isDirectory() && parent.exists()) {
                String[] list = parent.list();
                if (list == null || list.length == 0) {
                    parent.delete();
                }
            }
        }
    }

    public boolean isAvailable() {
        return file.exists() && file.canRead() && file.canWrite();
    }
}