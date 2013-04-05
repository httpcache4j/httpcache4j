package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.preference.Preference;
import org.junit.Test;
import org.junit.Assert;
import org.codehaus.httpcache4j.util.TestUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.net.URI;
import java.io.File;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Id $
 */
public class VaryTest {


    @Test
    public void testIsEmpty() {
        Vary vary = new Vary();
        assertTrue("Header names added", vary.isEmpty());
        vary = new Vary(new HashMap<String, String>());
        assertTrue("Header names added", vary.isEmpty());
    }

    @Test
    public void testHasOne() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Accept-Language", "en");
        Vary vary = new Vary(map);
        assertFalse("Header names added", vary.isEmpty());
        assertEquals("Header names added", 1, vary.size());
    }

    @Test
    public void testIsAuthenticated() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(HeaderConstants.AUTHORIZATION, "jdoe");
        Vary vary = new Vary(map);
        Assert.assertEquals("Header names added", 1, vary.size());
        Assert.assertTrue("Request does not match",
                vary.matches(new HTTPRequest(URI.create("hi")).challenge(new UsernamePasswordChallenge("jdoe", "bar"))));
    }

    @Test
    public void testHasMultiple() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Accept-Language", "en");
        map.put("Accept-Encoding", "gz");
        map.put("Accept-Charset", "UTF-8");
        Vary vary = new Vary(map);
        assertFalse("Header names added", vary.isEmpty());
        assertEquals("Header names added", 3, vary.size());
    }

    @Test
    public void testDoesNotMatchRequest() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Accept-Language", "en");
        map.put("Accept-Encoding", "gz");
        map.put("Accept-Charset", "UTF-8");
        Vary vary = new Vary(map);
        Assert.assertFalse("Header names added", vary.isEmpty());
        Assert.assertEquals("Header names added", 3, vary.size());
        HTTPRequest request = new HTTPRequest(URI.create("no.uri"));
        assertFalse(vary.matches(request));
    }

    @Test
    public void testDoesMatchesRequest() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Accept-Language", "en");
        map.put("Accept-Encoding", "gz");
        map.put("Accept-Charset", "UTF-8");
        Vary vary = new Vary(map);
        assertFalse("Header names added", vary.isEmpty());
        assertEquals("Header names added", 3, vary.size());

        HTTPRequest request = new HTTPRequest(URI.create("no.uri"));
        for (Map.Entry<String, String> header : map.entrySet()) {
            request = request.addHeader(header.getKey(), header.getValue());
        }
        assertTrue(vary.matches(request));
    }

    @Test
    public void testAlmostMatchesRequest() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Accept-Language", "en");
        map.put("Accept-Encoding", "gz");
        map.put("Accept-Charset", "UTF-8");
        Vary vary = new Vary(map);
        assertFalse("Header names added", vary.isEmpty());
        assertEquals("Header names added", 3, vary.size());
        HTTPRequest request = new HTTPRequest(URI.create("no.uri"));
        request = request.addHeader("Accept-Language", "de");
        request = request.addHeader("Accept-Encoding", "gz");
        request = request.addHeader("Accept-Charset", "UTF-8");

        assertFalse(vary.matches(request));
    }

    @Test
    public void acceptShouldMatch() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Accept", "application/xml");
        Vary vary = new Vary(map);
        assertFalse("Header names added", vary.isEmpty());
        assertEquals("Header names added", 1, vary.size());
        HTTPRequest request = new HTTPRequest(URI.create("no.uri"));
        request = request.addHeader("Accept", "application/xml");
        request = request.headers(new Headers().addAccept(MIMEType.valueOf("application/xml")));
        assertTrue("request did not specify application/xml", vary.matches(request));
    }

    @Test
    public void acceptWithManyPreferencesShouldMatch() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Accept", "application/xml, application/json");
        Vary vary = new Vary(map);
        assertFalse("Header names added", vary.isEmpty());
        assertEquals("Header names added", 1, vary.size());
        HTTPRequest request = new HTTPRequest(URI.create("no.uri"));
        request = request.addHeader("Accept", "application/xml");
        request = request.headers(new Headers().addAccept(MIMEType.valueOf("application/xml"), MIMEType.valueOf("application/json")));
        assertTrue("request did not specify application/xml", vary.matches(request));
    }

    @Test
    public void acceptWithUnsortedManyPreferencesShouldMatch() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Accept", "application/xml; q=0.8, application/json");
        Vary vary = new Vary(map);
        assertFalse("Header names added", vary.isEmpty());
        assertEquals("Header names added", 1, vary.size());
        HTTPRequest request = new HTTPRequest(URI.create("no.uri"));
        request = request.addHeader("Accept", "application/json, application/xml; q=0.8");
        assertTrue("Wrong accept header", vary.matches(request));
        request = request.setHeader("Accept", "application/xml; q=0.8, application/json");
        assertTrue("Wrong accept header", vary.matches(request));
    }

    @Test
    public void testVaryFileResolve() {
        FileManager resolver = new FileManager(TestUtil.getTestFile("target"));
        File file = resolver.resolve(Key.create(URI.create("foo"), new Vary()));
        File file2 = resolver.resolve(Key.create(URI.create("foo"), new Vary()));
        File file3 = resolver.resolve(Key.create(URI.create("foo"), new Vary(new HashMap<String, String>())));
        File file4 = resolver.resolve(Key.create(URI.create("foo"), new Vary(Collections.singletonMap("Accept-Language", "en"))));
        assertEquals(file.getAbsolutePath(), file2.getAbsolutePath());
        assertEquals(file.getAbsolutePath(), file3.getAbsolutePath());
        assertEquals(file2.getAbsolutePath(), file3.getAbsolutePath());
        assertEquals(file2.getAbsolutePath(), file3.getAbsolutePath());
        assertFalse(file4.getAbsolutePath().equals(file3.getAbsolutePath()));
    }
}
