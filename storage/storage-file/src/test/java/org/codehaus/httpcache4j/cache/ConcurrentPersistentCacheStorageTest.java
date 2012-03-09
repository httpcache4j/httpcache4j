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

import org.codehaus.httpcache4j.payload.FilePayload;
import org.codehaus.httpcache4j.util.TestUtil;
import org.codehaus.httpcache4j.util.DeletingFileFilter;
import org.codehaus.httpcache4j.HTTPResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:erlend@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class ConcurrentPersistentCacheStorageTest extends ConcurrentCacheStorageAbstractTest {

    @Test
    public void test100Concurrent2() throws InterruptedException {
        PersistentCacheStorage storage = (PersistentCacheStorage) cacheStorage;
        testIterations(100, 100);
        for (Key key : storage) {
            File file = storage.getFileManager().resolve(key);
            assertTrue(String.format("File %s did not exist ", file.getName()), file.exists());
        }
    }

    @Test
    public void test1001Concurrent() throws InterruptedException {
        PersistentCacheStorage storage = (PersistentCacheStorage) cacheStorage;
        testIterations(1001, 1000);
        for (Key key : storage) {
            File file = storage.getFileManager().resolve(key);
            assertTrue(String.format("File %s did not exist ", file.getParentFile().getName()), file.exists());
        }
    }

    @Override
    protected void assertResponse(final HTTPResponse response) {
        super.assertResponse(response);
        if (response.getPayload() instanceof FilePayload) {
            FilePayload payload = (FilePayload) response.getPayload();
            final File parent = payload.getFile().getParentFile();
            assertEquals(1, parent.list().length);
        }
    }

    protected CacheStorage createCacheStorage() {
        File storage = TestUtil.getTestFile("target/persistent/concurrent");
        return new PersistentCacheStorage(storage);
    }

}
