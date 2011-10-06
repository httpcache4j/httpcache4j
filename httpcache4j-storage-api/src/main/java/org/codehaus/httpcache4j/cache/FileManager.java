/*
 * Copyright (c) 2009. The Codehaus. All Rights Reserved.
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
 */

package org.codehaus.httpcache4j.cache;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.util.DeletingFileFilter;
import org.codehaus.httpcache4j.util.StorageUtil;

import java.io.*;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public final class FileManager implements Serializable {
    private static final long serialVersionUID = -5273056780013227862L;
    private final File baseDirectory;

    public FileManager(final File baseDirectory) {
        Validate.notNull(baseDirectory, "Base directory may not be null");
        this.baseDirectory = createFilesDirectory(baseDirectory);
    }

    private File createFilesDirectory(File baseDirectory) {
        File files = new File(baseDirectory, "files");
        StorageUtil.ensureDirectoryExists(files);
        return files;
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public synchronized File createFile(Key key, InputStream stream) throws IOException {
        File file = resolve(key);
        if (!file.getParentFile().exists()) {
            StorageUtil.ensureDirectoryExists(file.getParentFile());
        }
        FileOutputStream outputStream = FileUtils.openOutputStream(file);
        try {
            IOUtils.copy(stream, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        if (file.length() == 0) {
            file.delete();
            file = null;
        }
        if (file != null && !file.exists()) {
            throw new IOException(String.format("Failed to create File '%s' for Key: %s", file.getName(), key));
        }

        return file;
    }

    public synchronized void clear() {
        baseDirectory.listFiles(new DeletingFileFilter());
    }

    public synchronized void remove(Key key) {
        File resolved = resolve(key);
        if (resolved.delete() && directoryIsEmpty(resolved.getParentFile())) {
            resolved.getParentFile().delete();
        }
    }

    private boolean directoryIsEmpty(File directory) {
        if (directory.isDirectory()) {
            String[] list = directory.list();
            if (list == null || list.length == 0) {
                return true;
            }
        }
        return false;
    }

    public synchronized File resolve(Key key) {
        String uriHex = DigestUtils.md5Hex(key.getURI().toString());
        String vary;
        if (key.getVary().isEmpty()) {
            vary = "default";
        }
        else {
            vary = DigestUtils.md5Hex(key.getVary().toString());
        }
        File uriFolder = new File(baseDirectory, uriHex);
        return new File(uriFolder, vary);
    }
}
