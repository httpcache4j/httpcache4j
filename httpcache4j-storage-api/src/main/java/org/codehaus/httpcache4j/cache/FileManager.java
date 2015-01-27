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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import org.codehaus.httpcache4j.util.DeletingFileFilter;
import org.codehaus.httpcache4j.util.Digester;
import org.codehaus.httpcache4j.util.IOUtils;

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

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public synchronized File createFile(Key key, InputStream stream) throws IOException {
        File file = resolve(key);
        if (!file.getParentFile().exists()) {
            ensureDirectoryExists(file.getParentFile());
        }
        FileOutputStream to = new FileOutputStream(file);
        try {
            IOUtils.copy(stream, to);
        } finally {
            IOUtils.closeQuietly(stream);
            to.close();
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

    public synchronized File moveFile(File fromFile, Key to) throws IOException {
        File toFile = resolve(to);
        if (!toFile.getParentFile().exists()) {
            ensureDirectoryExists(toFile.getParentFile());
        }
        fromFile.renameTo(toFile);
        if (toFile.length() == 0) {
            toFile.delete();
            toFile = null;
        }
        if (toFile != null && !toFile.exists()) {
            throw new IOException(String.format("Failed to move File '%s' to File %s for Key: %s", fromFile.getName(), toFile.getName(), to));
        }

        return toFile;
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

    public synchronized void ensureDirectoryExists(File directory) {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalArgumentException(String.format("Directory %s did not exist, and could not be created", directory));
        }
    }

    public synchronized File resolve(Key key) {
        File uriFolder = resolve(key.getURI());
        String vary;
        if (key.getVary().isEmpty()) {
            vary = "default";
        }
        else {
            vary = Digester.md5(key.getVary().toString(), Charsets.UTF_8);
        }
        return new File(uriFolder, vary);
    }

    public synchronized File resolve(URI uri) {
        String uriHex = Digester.md5(uri.toString(), Charsets.UTF_8);
        String distribution = uriHex.substring(0, 2);
        return new File(new File(baseDirectory, distribution), uriHex);
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

    private File createFilesDirectory(File baseDirectory) {
        File files = new File(baseDirectory, "files");
        ensureDirectoryExists(files);
        return files;
    }
}
