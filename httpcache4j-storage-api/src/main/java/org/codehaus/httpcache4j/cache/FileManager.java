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
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import net.hamnaberg.funclite.Preconditions;
import org.codehaus.httpcache4j.util.DeletingFileVisitor;
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
        try (InputStream is = stream; FileOutputStream to = new FileOutputStream(file)) {
            IOUtils.copy(is, to);
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
        Files.move(fromFile.toPath(), toFile.toPath());
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
        deleteDirectory(baseDirectory);
    }

    public synchronized void remove(Key key) {
        File resolved = resolve(key);
        if (resolved.delete() && directoryIsEmpty(resolved.getParentFile())) {
            resolved.getParentFile().delete();
        }
    }

    public synchronized void clear(URI uri) {
        File resolved = resolve(uri);
        deleteDirectory(resolved);
        if (directoryIsEmpty(resolved.getParentFile())) {
            resolved.getParentFile().delete();
        }
    }

    private void deleteDirectory(File resolved) {
        try {
            Files.walkFileTree(resolved.toPath(), new DeletingFileVisitor());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void ensureDirectoryExists(File directory) {
        try {
            Files.createDirectories(directory.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized File resolve(Key key) {
        File uriFolder = resolve(key.getURI());
        String vary;
        if (key.getVary().isEmpty()) {
            vary = "default";
        }
        else {
            vary = Digester.md5(key.getVary().toString(), StandardCharsets.UTF_8);
        }
        return new File(uriFolder, vary);
    }

    public synchronized File resolve(URI uri) {
        String uriHex = Digester.md5(uri.toString(), StandardCharsets.UTF_8);
        String distribution = uriHex.substring(0, 2);
        return new File(new File(baseDirectory, distribution), uriHex);
    }

    private boolean directoryIsEmpty(File directory) {
        try {
            return !Files.list(directory.toPath()).findAny().isPresent();
        } catch (IOException e) {
            return false;
        }
    }

    private File createFilesDirectory(File baseDirectory) {
        File files = new File(baseDirectory, "files");
        ensureDirectoryExists(files);
        return files;
    }
}
