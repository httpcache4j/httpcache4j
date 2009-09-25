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

package org.codehaus.httpcache4j;

import org.junit.*;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.cache.MemoryCacheStorage;
import org.codehaus.httpcache4j.resolver.HTTPClientResponseResolver;
import org.codehaus.httpcache4j.payload.FilePayload;
import org.codehaus.httpcache4j.util.TestUtil;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.URI;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class SimpleCacheIntegrationTest {
    private static Server server;
    
    private static URI baseRequestURI;
    private static final String TEST_FILE = "testFile";
    private HTTPCache cache;
    private MemoryCacheStorage storage;

    @BeforeClass
    public static void setupServer() {
        baseRequestURI = URI.create(String.format("http://localhost:%s/", Server.PORT));        
        server = new Server();
        server.start();
    }

    @Before
    public void before() {
        storage = new MemoryCacheStorage();
        cache = new HTTPCache(storage, new HTTPClientResponseResolver(new DefaultHttpClient()));
        HTTPRequest req = new HTTPRequest(baseRequestURI.resolve(TEST_FILE), HTTPMethod.PUT);
        req = req.payload(new FilePayload(TestUtil.getTestFile("pom.xml"), MIMEType.valueOf("application/xml")));
        cache.doCachedRequest(req);
    }

    @Test
    public void simpleGET() {
        HTTPResponse response = cache.doCachedRequest(new HTTPRequest(baseRequestURI.resolve(TEST_FILE)));
        Assert.assertNull(response.getETag());
        Assert.assertNull(response.getLastModified());
        Assert.assertEquals(response.getStatus(), Status.OK);
        Assert.assertEquals(0, storage.size());
    }

    @Test
    public void simpleGETWithETagResponse() {
        HTTPResponse response = cache.doCachedRequest(new HTTPRequest(baseRequestURI.resolve(String.format("etag/%s", TEST_FILE))));
        Assert.assertNotNull(response.getETag());
        Assert.assertNull(response.getLastModified());
        Assert.assertEquals(response.getStatus(), Status.OK);
        Assert.assertEquals(1, storage.size());
    }

    @AfterClass
    public static void shutdownServer() {
        if (server != null) {
            server.stop();
        }
    }
}
