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

import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.httpcache4j.cache.CacheStorage;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.payload.FilePayload;
import org.codehaus.httpcache4j.resolver.HTTPClientResponseResolver;
import org.codehaus.httpcache4j.resolver.ResponseResolver;
import org.codehaus.httpcache4j.util.ResponseWriter;
import org.codehaus.httpcache4j.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.PrintWriter;
import java.net.URI;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public abstract class AbstractCacheIntegrationTest {
    private static JettyServer server;
    private static URI baseRequestURI;
    private static final String TEST_FILE = "testFile";
    private HTTPCache cache;
    private CacheStorage storage;

    @BeforeClass
    public static void setupServer() {
        baseRequestURI = URI.create(String.format("http://localhost:%s/", JettyServer.PORT));
        server = new JettyServer();
        server.start();
    }

    @Before
    public void before() {
        storage = createStorage();
        cache = new HTTPCache(storage, new HTTPClientResponseResolver(new DefaultHttpClient()));
        HTTPRequest req = new HTTPRequest(baseRequestURI.resolve(TEST_FILE), HTTPMethod.PUT);
        req = req.payload(new FilePayload(TestUtil.getTestFile("pom.xml"), MIMEType.valueOf("application/xml")));
        cache.doCachedRequest(req);
    }

    protected abstract CacheStorage createStorage();

    @Test
    public void GETNotCacheableResponse() {
        HTTPResponse response = get(baseRequestURI.resolve(TEST_FILE));
        assertNull(response.getETag());
        assertNull(response.getLastModified());
        assertEquals(Status.OK, response.getStatus());
        assertEquals(0, storage.size());
    }

    @Test
    public void GETWithETagResponse() {
        HTTPResponse response = get(baseRequestURI.resolve(String.format("etag/%s", TEST_FILE)));
        assertNotNull(response.getETag());
        assertNull(response.getLastModified());
        assertEquals(Status.OK, response.getStatus());
        assertEquals(1, storage.size());
    }

    @Test
    public void GETWithETagThenPUTAndReGETResponse() {
        URI uri = baseRequestURI.resolve(String.format("etag/%s", TEST_FILE));
        HTTPResponse response = get(uri);
        assertNotNull(response.getETag());
        assertNull(response.getLastModified());
        assertEquals(Status.OK, response.getStatus());
        
        assertEquals(1, storage.size());
        response = cache.doCachedRequest(new HTTPRequest(uri, HTTPMethod.PUT));
        assertEquals(0, storage.size());
        assertEquals(Status.NO_CONTENT, response.getStatus());
        response = get(uri);
        assertNotNull(response.getETag());
        assertNull(response.getLastModified());
        assertEquals(Status.OK, response.getStatus());
        assertEquals(1, storage.size());
    }

    @Test
    public void GETWithBasicAuthentication() {
        URI uri = baseRequestURI.resolve(String.format("etag/basic,u=u,p=p/%s", TEST_FILE));
        HTTPResponse response = get(uri);
        assertEquals(Status.UNAUTHORIZED, response.getStatus());
        response.consume();
        HTTPRequest request = new HTTPRequest(uri).challenge(new UsernamePasswordChallenge("u", "p"));
        response = cache.doCachedRequest(request);
        HTTPClientResponseResolver responseResolver = (HTTPClientResponseResolver) cache.getResolver();
        assertTrue(responseResolver.isPreemptiveAuthenticationEnabled());
        assertEquals(Status.OK, response.getStatus());
        response.consume();
    }

    @Test
    public void GETWithDigestAuthentication() {
        URI uri = baseRequestURI.resolve(String.format("etag/digest,u=u,p=p/%s", TEST_FILE));
        HTTPResponse response = get(uri);
        assertEquals(Status.UNAUTHORIZED, response.getStatus());
        response.consume();
        HTTPRequest request = new HTTPRequest(uri).challenge(new UsernamePasswordChallenge("u", "p"));
        response = cache.doCachedRequest(request);
        assertEquals(Status.OK, response.getStatus());
        response.consume();
    }
    
    private HTTPResponse get(URI uri) {
        HTTPRequest getRequest = new HTTPRequest(uri);
        HTTPResponse response = cache.doCachedRequest(getRequest);
        assertNotNull(response);
        assertFalse(response.getStatus().equals(Status.INTERNAL_SERVER_ERROR));
        return response;
    }
}
