/*
 * Copyright (c) 2008, The Codehaus. All Rights Reserved.
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
 *
 */

package org.codehaus.httpcache4j.cache;

import static junit.framework.Assert.assertEquals;
import junit.framework.Assert;

import java.io.File;
import java.net.URI;

import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.Status;
import org.codehaus.httpcache4j.util.DeletingFileFilter;
import org.codehaus.httpcache4j.util.TestUtil;
import org.junit.Test;

/** @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a> */
public class PersistentCacheStorageTest extends CacheStorageAbstractTest {
    private File baseDirectory;
    
    @Override
	protected CacheStorage createCacheStorage() {
        baseDirectory = TestUtil.getTestFile("target/test/");
        baseDirectory.mkdirs();
        return new PersistentCacheStorage(baseDirectory);
    }

    @Test
    public void testPUTWithRealPayload() throws Exception {
        File tempFile = File.createTempFile("foo", "bar", baseDirectory);
        tempFile.deleteOnExit();
        HTTPResponse response = new HTTPResponse(new CleanableFilePayload(tempFile, MIMEType.APPLICATION_OCTET_STREAM), Status.OK, new Headers());
        storage.insert(Key.create(URI.create("foo"), new Vary()), response);
        Assert.assertEquals(1, storage.size());
    }

    @Override
	public void afterTest() {
        baseDirectory.listFiles(new DeletingFileFilter());
    }
}