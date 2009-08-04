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

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.resolver.StoragePolicy;
import org.codehaus.httpcache4j.util.DeletingFileFilter;
import org.codehaus.httpcache4j.util.Pair;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;

import java.util.*;
import java.io.*;
import java.net.URI;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
final class FileManager implements StoragePolicy {
    private final FileResolver fileResolver;

    public FileManager(final File baseDirectory) {
        Validate.notNull(baseDirectory, "Base directory may not be null");
        ensureDirectoryExists(baseDirectory);
        File files = new File(baseDirectory, "files");
        ensureDirectoryExists(files);
        this.fileResolver = new FileResolver(files);
    }

    static void ensureDirectoryExists(File directory) {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalArgumentException(String.format("Directory %s did not exist, and could not be created", directory));
        }
    }


    File createFile(HTTPRequest request, InputStream stream) throws IOException {
        File file = fileResolver.resolve(request.getRequestURI(), UUID.randomUUID().toString());

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

        return file;
    }

/*
    private void removeUnknownFiles(CacheStorage storage) {
        List<File> knownFiles = new ArrayList<File>();
        List<Pair<URI, CacheItem>> invalidations = new ArrayList<Pair<URI, CacheItem>>();
        for (Key key : storage) {
            for (Map.Entry<Vary, CacheItem> entry : cacheValue.getValue()) {
                HTTPResponse response = entry.getValue().getResponse();
                if (response.hasPayload()) {
                    if (response.getPayload() instanceof CleanableFilePayload) {
                        CleanableFilePayload payload = (CleanableFilePayload) response.getPayload();
                        if (payload.getFile().exists()) {
                            knownFiles.add(payload.getFile());
                        }
                        else {
                          invalidations.add(Pair.of(cacheValue.getKey(), entry.getValue()));
                        }
                    }
                }
            }
        }
        for (Map.Entry<URI, CacheItem> invalidation : invalidations) {
            storage.invalidate(invalidation.getKey(), invalidation.getValue());
        }
        fileResolver.getBaseDirectory().listFiles(new DeletingFileFilter(knownFiles));        
    }
*/
}
