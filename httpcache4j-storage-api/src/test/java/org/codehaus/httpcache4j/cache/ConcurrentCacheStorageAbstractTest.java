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

import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.codehaus.httpcache4j.util.NullInputStream;
import org.junit.After;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:erlend@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public abstract class ConcurrentCacheStorageAbstractTest {
    private ExecutorService service = Executors.newFixedThreadPool(4);
    protected CacheStorage cacheStorage;

    @Before
    public void setUp() {
        cacheStorage = createCacheStorage();
    }

    protected abstract CacheStorage createCacheStorage();

    @Test
    public void test100Concurrent() throws InterruptedException {
        testIterations(100, 100);
    }

    protected void testIterations(int numberOfIterations, int expected) throws InterruptedException {
        List<Callable<HTTPResponse>> calls = new ArrayList<Callable<HTTPResponse>>();
        for (int i = 1; i <= numberOfIterations; i++) {
            final URI uri = URI.create(String.valueOf(i));
            final HTTPRequest request = new HTTPRequest(uri);
            Callable<HTTPResponse> call = new Callable<HTTPResponse>() {
                public HTTPResponse call() throws Exception {
                    HTTPResponse cached = cacheStorage.insert(request, createCacheResponse());
                    assertResponse(cached);                    
                    CacheItem cacheItem = cacheStorage.get(request);
                    HTTPResponse response = cacheItem.getResponse();
                    assertResponse(response);
                    cached = cacheStorage.update(request, createCacheResponse());
                    assertNotSame(cached, cacheItem.getResponse());
                    assertResponse(cached);
                    return cached;
                }
            };
            calls.add(call);
        }
        List<Future<HTTPResponse>> responses = service.invokeAll(calls);
        for (Future<HTTPResponse> responseFuture : responses) {
            try {
                responseFuture.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
                fail(e.getCause().getMessage());
            }
        }
        assertEquals(expected, cacheStorage.size());
    }

    @Test
    public void test1000InsertsOfSameURI() throws InterruptedException {
        final HTTPRequest request = new HTTPRequest(URI.create("GET"));
        List<Callable<HTTPResponse>> calls = new ArrayList<Callable<HTTPResponse>>();
        for (int i = 0; i < 1000; i++) {
            calls.add(new Callable<HTTPResponse>() {
                public HTTPResponse call() throws Exception {
                    return cacheStorage.insert(request, createCacheResponse());
                }
            });
        }

        List<Future<HTTPResponse>> responses = service.invokeAll(calls);
        for (Future<HTTPResponse> response : responses) {
            try {
                HTTPResponse real = response.get();
                assertResponse(real);
            } catch (ExecutionException e) {
                e.printStackTrace();
                fail(e.getCause().getMessage());
            }
        }
        assertEquals(1, cacheStorage.size());
    }


    private HTTPResponse createCacheResponse() {
        return new HTTPResponse(new InputStreamPayload(new NullInputStream(40), MIMEType.APPLICATION_OCTET_STREAM), Status.OK, new Headers().add("Foo", "Bar"));
    }

    protected void assertResponse(final HTTPResponse response) {
        assertNotNull("Response was null", response);
        assertTrue("Payload was not here", response.hasPayload());
        assertTrue("Payload was not available", response.getPayload().isAvailable());
        InputStream is = response.getPayload().getInputStream();
        try {
            CharStreams.toString(new InputStreamReader(is));
        } catch (IOException e) {
            e.printStackTrace();
            fail("unable to create string from stream");
        }
        finally {
            Closeables.closeQuietly(is);
        }
    }

    @After
    public void tearDown() {
        if (cacheStorage != null) {
            cacheStorage.clear();
        }
        service.shutdownNow();
    }
}
