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

package org.codehaus.httpcache4j.storage;

import org.junit.*;
import org.codehaus.httpcache4j.util.TestUtil;
import org.codehaus.httpcache4j.util.DeletingFileFilter;
import org.codehaus.httpcache4j.cache.Key;
import org.codehaus.httpcache4j.cache.Vary;
import org.codehaus.httpcache4j.cache.CacheStorageAbstractTest;
import org.codehaus.httpcache4j.cache.CacheStorage;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.Status;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.apache.commons.io.input.NullInputStream;

import java.io.File;
import java.net.URI;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class DerbyStorageTestCase extends CacheStorageAbstractTest{
    private static File storageDirectory;

    @BeforeClass
    public static void beforeClass() {
        storageDirectory = TestUtil.getTestFile("target/storage");
    }

    @AfterClass
    public static void afterClass() {
        storageDirectory.listFiles(new DeletingFileFilter());
    }

    protected CacheStorage createCacheStorage() {
        return new DerbyCacheStorage(storageDirectory);
    }

    @Override
    protected void afterTest() {
    }
}
