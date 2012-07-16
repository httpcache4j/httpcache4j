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

import java.io.File;
import java.net.URI;

import junit.framework.Assert;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.Status;
import org.codehaus.httpcache4j.payload.FilePayload;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.codehaus.httpcache4j.util.NullInputStream;
import org.codehaus.httpcache4j.util.TestUtil;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/** @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a> */
public class PersistentCacheStorage2Test extends CacheStorageAbstractTest {

    @Override
	protected CacheStorage createCacheStorage() {
        return new PersistentCacheStorage2(TestUtil.getTestFile("target/persistent2"));
    }

    @Test
    public void testPUTWithRealPayload() throws Exception {
        HTTPResponse response = createRealResponse();
        storage.insert(REQUEST, response);
        Assert.assertEquals(1, storage.size());
    }

    @Test
    public void testMultipleInserts() {
        Key key = new Key(URI.create("foo"), new Vary());
        HTTPResponse res = null;
        for (int i = 0; i < 100; i++) {
            HTTPResponse response = createRealResponse();
            res = storage.insert(REQUEST, response);
        }
        assertNotNull("Result may not be null", res);
        if (res.hasPayload()) {
            PersistentCacheStorage2 cacheStorage = (PersistentCacheStorage2) storage;
            FilePayload payload = (FilePayload) res.getPayload();
            final File parent = payload.getFile().getParentFile();
            final File file = cacheStorage.getFileManager().resolve(key);
            assertEquals(file.toString(), payload.getFile().toString());
            assertTrue(parent.isDirectory());
            assertEquals(2, parent.list().length);
        }
    }

    @Test
    public void testMANYURIInserts() {
        for (int i = 0; i < 100; i++) {
            URI uri = URI.create("foo" + i);
            HTTPResponse response = createRealResponse();
            storage.insert(new HTTPRequest(uri), response);
        }
        assertEquals(100, storage.size());
    }

    private HTTPResponse createRealResponse() {
        return new HTTPResponse(new InputStreamPayload(new NullInputStream(10), MIMEType.APPLICATION_OCTET_STREAM), Status.OK, new Headers());
    }

    @Override
	public void afterTest() {
    }
}
