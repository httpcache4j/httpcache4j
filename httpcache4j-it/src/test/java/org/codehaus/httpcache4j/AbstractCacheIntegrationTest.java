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

import java.io.File;

import org.codehaus.httpcache4j.payload.StringPayload;
import org.codehaus.httpcache4j.resolver.ResponseResolver;
import org.codehaus.httpcache4j.util.TestUtil;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.codehaus.httpcache4j.cache.CacheStorage;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.payload.FilePayload;
import org.codehaus.httpcache4j.payload.ByteArrayPayload;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.net.URI;
import java.time.LocalDateTime;

import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public abstract class AbstractCacheIntegrationTest {
    private static URI baseRequestURI;
    private static URI baseCustomRequestURI;
    private static final String TEST_FILE = "testFile";
    private HTTPCache cache;
    private CacheStorage storage;
    private static Server jettyServer;

    @BeforeClass
    public static void setupServer() throws Exception {
        baseRequestURI = URI.create(String.format("http://localhost:%s/testbed/", JettyServer.PORT));
        baseCustomRequestURI = URI.create(String.format("http://localhost:%s/custom/", JettyServer.PORT));
        System.out.println("::: Starting server :::");
        jettyServer = new Server(JettyServer.PORT);
        final String webapp = "target/testbed/";
        if (!TestUtil.getTestFile(webapp).exists()) {
            throw new IllegalStateException("WebApp dir does not exist!");
        }
        HandlerList handlerList = new HandlerList();
        Handler webAppHandler = new WebAppContext(webapp, "/testbed");
        handlerList.addHandler(webAppHandler);
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/custom");
        contextHandler.addServlet(VaryResourceServlet.class, "/*");
        handlerList.addHandler(contextHandler);
        jettyServer.setHandler(handlerList);
        jettyServer.start();
    }

    @AfterClass
    public static void shutdownServer()
            throws Exception {
        System.out.println("::: Stopping server :::");
        jettyServer.stop();
    }

    @Before
    public void before() {
        storage = createStorage();
        cache = new HTTPCache(storage, createReponseResolver());
        HTTPRequest req = new HTTPRequest(baseRequestURI.resolve(TEST_FILE), HTTPMethod.PUT);
        req = req.payload(new FilePayload(new File("pom.xml"), MIMEType.valueOf("application/xml")));
        cache.execute(req);
    }

    protected abstract ResponseResolver createReponseResolver();

    protected abstract CacheStorage createStorage();

    @Test
    public void POSTWithContentLength() {
        HTTPRequest req = new HTTPRequest(baseCustomRequestURI, HTTPMethod.POST).addHeader(HeaderConstants.CONTENT_LENGTH, "1000");
        req = req.payload(new StringPayload("test", MIMEType.valueOf("text/plain")));
        HTTPResponse response = cache.execute(req);
        assertEquals(Status.OK, response.getStatus());
    }

    @Test
    public void GETNotCacheableResponse() {
        HTTPResponse response = get(baseRequestURI.resolve(TEST_FILE));
        assertNull(response.getHeaders().getETag().orElse(null));
        assertNull(response.getHeaders().getLastModified().orElse(null));
        assertEquals(Status.OK, response.getStatus());
        assertEquals(0, storage.size());
        assertEquals(CacheHeaderBuilder.getBuilder().createMISSXCacheHeader(), response.getHeaders().getFirstHeader(HeaderConstants.X_CACHE).get());
    }

    @Test
    public void GETWithETagResponse() {
        HTTPResponse response = get(baseRequestURI.resolve(String.format("etag/%s", TEST_FILE)));
        assertNotNull(response.getHeaders().getETag().orElse(null));
        assertNull(response.getHeaders().getLastModified().orElse(null));
        assertEquals(Status.OK, response.getStatus());
        assertEquals(1, storage.size());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
    }

    @Test
    public void GETWithETagThenPUTAndReGETResponse() {
        URI uri = baseRequestURI.resolve(String.format("etag/%s", TEST_FILE));
        HTTPResponse response = get(uri);
        assertNotNull(response.getHeaders().getETag().orElse(null));
        assertNull(response.getHeaders().getLastModified().orElse(null));
        assertEquals(Status.OK, response.getStatus());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE).orElse(null));

        assertEquals(1, storage.size());
        response = cache.execute(new HTTPRequest(uri, HTTPMethod.PUT).withPayload(new StringPayload("test", MIMEType.valueOf("text/plain"))));
        assertEquals(0, storage.size());
        assertEquals(Status.NO_CONTENT, response.getStatus());
        assertNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE).orElse(null));
        response = get(uri);
        assertNotNull(response.getHeaders().getETag().orElse(null));
        assertNull(response.getHeaders().getLastModified().orElse(null));
        assertEquals(Status.OK, response.getStatus());
        assertEquals(1, storage.size());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
    }

    @Test
    public void GETWithBasicAuthentication() {
        URI uri = baseRequestURI.resolve(String.format("etag/basic,u=u,p=p/%s", TEST_FILE));
        HTTPResponse response = get(uri);
        assertEquals(Status.UNAUTHORIZED, response.getStatus());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
        response.consume();
        HTTPRequest request = new HTTPRequest(uri).withChallenge(new UsernamePasswordChallenge("u", "p"));
        response = cache.execute(request);
        assertEquals(Status.OK, response.getStatus());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
        response.consume();
    }

    @Test
    public void PUTWithBasicAuthentication() throws Exception {
        URI uri = baseRequestURI.resolve(String.format("etag/basic,u=u,p=p/%s", TEST_FILE));
        HTTPResponse response = doRequest(uri, HTTPMethod.PUT);
        assertEquals(Status.UNAUTHORIZED, response.getStatus());
        response.consume();
        HTTPRequest request = new HTTPRequest(uri, HTTPMethod.PUT).withPayload(new StringPayload("test", MIMEType.valueOf("text/plain"))).withChallenge(new UsernamePasswordChallenge("u", "p"))
                .withPayload(new ByteArrayPayload(new FileInputStream(new File("pom.xml")), MIMEType.valueOf("application/xml")));
        response = cache.execute(request);
        assertEquals(Status.NO_CONTENT, response.getStatus());
        response.consume();
    }

    @Test
    public void GETWithDigestAuthentication() {
        URI uri = baseRequestURI.resolve(String.format("etag/digest,u=u,p=p/%s", TEST_FILE));
        HTTPResponse response = get(uri);
        assertEquals(Status.UNAUTHORIZED, response.getStatus());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
        response.consume();
        HTTPRequest request = new HTTPRequest(uri).challenge(new UsernamePasswordChallenge("u", "p"));
        response = cache.execute(request);
        assertEquals(Status.OK, response.getStatus());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
        response.consume();
    }

    @Test
    public void GETWithLastModified() {
        URI uri = baseRequestURI.resolve(String.format("lm/%s", TEST_FILE));
        HTTPResponse response = get(uri);
        assertEquals(Status.OK, response.getStatus());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
        assertFalse(response.isCached());
        response.consume();
        response = get(uri);
        assertEquals(Status.OK, response.getStatus());
        assertTrue(response.isCached());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
        response.consume();
    }

    @Test
    public void GETWithLastModifiedAndETag() {
        URI uri = baseRequestURI.resolve(String.format("lm/etag/%s", TEST_FILE));
        HTTPResponse response = get(uri);
        assertEquals(Status.OK, response.getStatus());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
        assertFalse(response.isCached());
        response.consume();
        response = get(uri);
        assertEquals(Status.OK, response.getStatus());
        assertTrue(response.isCached());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
        response.consume();
    }

    @Test
    public void GETWithVaryForAccept() {
        HTTPRequest request = new HTTPRequest(baseCustomRequestURI, HTTPMethod.GET).addHeader(HeaderConstants.ACCEPT, "text/plain");
        HTTPResponse response = cache.execute(request);
        assertEquals(Status.OK, response.getStatus());
        assertTrue(MIMEType.valueOf("text/plain").includes(response.getPayload().get().getMimeType()));
        assertFalse(response.isCached());
        response.consume();
        response = cache.execute(request);
        assertTrue(MIMEType.valueOf("text/plain").includes(response.getPayload().get().getMimeType()));
        assertTrue(response.isCached());
        response.consume();
        request = new HTTPRequest(baseCustomRequestURI, HTTPMethod.GET).addHeader(HeaderConstants.ACCEPT, "text/xml");
        response = cache.execute(request);
        response.consume();
        assertEquals(Status.OK, response.getStatus());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
        assertFalse(response.isCached());
        assertTrue(MIMEType.valueOf("text/xml").includes(response.getPayload().get().getMimeType()));
        response = cache.execute(request);
        response.consume();
        assertEquals(Status.OK, response.getStatus());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
        assertTrue(response.isCached());
        assertTrue(MIMEType.valueOf("text/xml").includes(response.getPayload().get().getMimeType()));
        assertEquals(2, storage.size());
    }

    /**
     * Tests that requests come from the cache, but that the 
     * response isn't specified that the cache is used when the response
     * is generated from the server
     */
    @Test
    public void GETWithMaxAge() {
        URI uri = baseRequestURI.resolve(String.format("cc,10/%s", TEST_FILE));
        HTTPResponse response = get(uri);
//        System.out.println(response.getHeaders());

        assertEquals(Status.OK, response.getStatus());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
        LocalDateTime originalDate = response.getHeaders().getDate().orElse(null);
        assertTrue(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE).get().contains("MISS"));
        assertFalse(response.isCached());
        response.consume();
        try {
        	Thread.sleep(5000);
        } catch (Exception e) {}
        response = get(uri);
        LocalDateTime cacheDate = response.getHeaders().getDate().orElse(null);
        assertEquals(Status.OK, response.getStatus());
        assertTrue(originalDate.equals(cacheDate));

        assertTrue(response.isCached());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
        assertTrue(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE).get().contains("HIT"));
        response.consume();
        
        // sleep here.  The response should come from the server
        try {
        	Thread.sleep(12000);
        } catch (Exception e) {}
        response = get(uri);
        LocalDateTime nonCacheDate = response.getHeaders().getDate().orElse(null);
        
        assertEquals(Status.OK, response.getStatus());
        assertFalse(originalDate.equals(nonCacheDate));
        assertFalse(response.isCached());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
        assertTrue(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE).get().contains("MISS"));
        response.consume();
        
        try {
        	Thread.sleep(2000);
        } catch (Exception e) {}
        response = get(uri);
        LocalDateTime shouldBeAboveNonCacheDate = response.getHeaders().getDate().orElse(null);
        
        assertEquals(Status.OK, response.getStatus());
        assertTrue(nonCacheDate.equals(shouldBeAboveNonCacheDate));
        assertTrue(response.isCached());
        assertNotNull(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE));
        assertTrue(response.getHeaders().getFirstHeaderValue(HeaderConstants.X_CACHE).get().contains("HIT"));
        response.consume();
    }

    private HTTPResponse get(URI uri) {
        return doRequest(uri, HTTPMethod.GET);
    }

    private HTTPResponse doRequest(URI uri, HTTPMethod method) {
        HTTPRequest request = new HTTPRequest(uri, method);
        if (method.canHavePayload()) {
            request = request.withPayload(new StringPayload("test", MIMEType.valueOf("text/plain")));
        }
        HTTPResponse response = cache.execute(request);
        assertNotNull(response);
        assertFalse(response.getStatus().equals(Status.INTERNAL_SERVER_ERROR));
        return response;
    }
}
