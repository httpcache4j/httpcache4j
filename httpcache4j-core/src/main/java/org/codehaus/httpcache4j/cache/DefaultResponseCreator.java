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

import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.resolver.AbstractResponseCreator;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class DefaultResponseCreator extends AbstractResponseCreator {

    public DefaultResponseCreator(final File baseDirectory, CacheStorage storage) {
        super(new FileManager(baseDirectory, storage));
    }

    @Override
    protected FileManager getStoragePolicy() {
        return (FileManager) super.getStoragePolicy();
    }

    protected final Payload createCachedPayload(HTTPRequest request, Headers responseHeaders, InputStream stream, MIMEType type) throws IOException {
        if (stream != null) {
            File file = getStoragePolicy().createFile(request, stream);
            if (file != null && file.exists()) {
                return new CleanableFilePayload(file, type);
            }
        }
        return null;
    }
}
