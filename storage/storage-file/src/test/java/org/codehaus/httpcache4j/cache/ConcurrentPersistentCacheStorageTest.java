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

import org.codehaus.httpcache4j.util.TestUtil;
import org.codehaus.httpcache4j.util.DeletingFileFilter;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.File;

/**
 * @author <a href="mailto:erlend@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class ConcurrentPersistentCacheStorageTest extends ConcurrentCacheStorageAbstractTest {
    private File storage;

    @Override
    public void setUp() {
        storage = TestUtil.getTestFile("target/persistent");
        super.setUp();
    }

    @Test
    public void test100Concurrent2() throws InterruptedException {
        testIterations(100, 100);
        File files = new File(storage, "files");
        assertEquals(files.list().length, cacheStorage.size());
    }

    @Test
    public void test1001Concurrent() throws InterruptedException {
        testIterations(1001, 1000);
    }
    

    @Override
    public void tearDown() {
        storage.listFiles(new DeletingFileFilter());        
        super.tearDown();
    }

    protected CacheStorage createCacheStorage() {
        return new PersistentCacheStorage(storage);
    }

}
