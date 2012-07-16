/*
 * Copyright (c) 2010. The Codehaus. All Rights Reserved.
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

package org.codehaus.httpcache4j.storage.jdbc;

import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.Status;
import org.codehaus.httpcache4j.cache.CacheStorage;
import org.codehaus.httpcache4j.cache.CacheStorageAbstractTest;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.codehaus.httpcache4j.util.DeletingFileFilter;
import org.codehaus.httpcache4j.util.NullInputStream;
import org.codehaus.httpcache4j.util.TestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:erlend@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class H2CacheStorageTestCase extends CacheStorageAbstractTest{
    private static File storageDirectory;

    @BeforeClass
    public static void beforeClass() {
        storageDirectory = TestUtil.getTestFile("target/storage/single");
    }

    @AfterClass
    public static void afterClass() {
        if (storageDirectory != null) {
            storageDirectory.listFiles(new DeletingFileFilter());
        }
    }

    protected CacheStorage createCacheStorage() {
        return new H2CacheStorage(storageDirectory);
    }

    @Override
    protected void afterTest() {
    }

    @Test
    public void testPUTWithRealPayload() throws Exception {
        HTTPResponse response = new HTTPResponse(new InputStreamPayload(new NullInputStream(10), MIMEType.APPLICATION_OCTET_STREAM), Status.OK, new Headers());
        assertEquals(0, storage.size());
        HTTPResponse res = storage.insert(REQUEST, response);
        res.consume();
        assertEquals(1, storage.size());
    }

}