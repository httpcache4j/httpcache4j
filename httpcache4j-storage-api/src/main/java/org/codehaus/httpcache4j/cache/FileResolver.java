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

import org.apache.commons.lang.Validate;
import org.apache.commons.codec.digest.DigestUtils;
import org.codehaus.httpcache4j.util.StorageUtil;

import java.net.URI;
import java.io.File;
import java.io.Serializable;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
class FileResolver implements Serializable {
    private static final long serialVersionUID = 3986711605116911333L;
    private final File baseDirectory;

    public FileResolver(File baseDirectory) {
        Validate.notNull(baseDirectory, "Base directory may not be null");
        this.baseDirectory = baseDirectory;
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public File resolve(Key key) {
        String uriSha = DigestUtils.shaHex(key.getURI().toString());
        String varySha = DigestUtils.shaHex(key.getVary().toString());
        File uriFolder = new File(baseDirectory, uriSha);
        StorageUtil.ensureDirectoryExists(uriFolder);
        return new File(uriFolder, varySha);
    }
}
