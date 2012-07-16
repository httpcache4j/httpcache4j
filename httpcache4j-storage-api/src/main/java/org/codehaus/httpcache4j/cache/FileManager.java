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

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;
import org.codehaus.httpcache4j.util.DeletingFileFilter;
import org.codehaus.httpcache4j.util.StorageUtil;

import java.io.*;
import java.net.URI;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public final class FileManager implements Serializable {
    private static final long serialVersionUID = -5273056780013227862L;
    private final File baseDirectory;

    public FileManager(final File baseDirectory) {
        Preconditions.checkNotNull(baseDirectory, "Base directory may not be null");
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
        OutputSupplier<FileOutputStream> outputStream = Files.newOutputStreamSupplier(file);
        try {
            ByteStreams.copy(stream, outputStream);
        } finally {
            Closeables.closeQuietly(stream);
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

    public synchronized void clear(URI uri) {
        File resolved = resolve(uri);
        resolved.listFiles(new DeletingFileFilter());
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
        File uriFolder = resolve(key.getURI());
        String vary;
        if (key.getVary().isEmpty()) {
            vary = "default";
        }
        else {
            vary = Hashing.md5().hashString(key.getVary().toString(), Charsets.UTF_8).toString().trim();
        }
        return new File(uriFolder, vary);
    }

    public synchronized File resolve(URI uri) {
        String uriHex = Hashing.md5().hashString(uri.toString(), Charsets.UTF_8).toString().trim();
        String distribution = uriHex.substring(0, 2);
        return new File(new File(baseDirectory, distribution), uriHex);
    }
}
