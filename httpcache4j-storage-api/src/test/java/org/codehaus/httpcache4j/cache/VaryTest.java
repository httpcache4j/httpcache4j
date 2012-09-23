package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.preference.Preference;
import org.junit.Test;
import org.junit.Assert;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.util.TestUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.net.URI;
import java.io.File;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Id $
 */
public class VaryTest {
    private Vary vary;

    @Test
    public void testIsEmpty() {
        vary = new Vary();
        Assert.assertTrue("Header names added", vary.isEmpty());
        vary = new Vary(new HashMap<String, String>());
        Assert.assertTrue("Header names added", vary.isEmpty());
    }

    @Test
    public void testHasOne() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Accept-Language", "en");
        vary = new Vary(map);
        Assert.assertFalse("Header names added", vary.isEmpty());
        Assert.assertEquals("Header names added", 1, vary.size());
    }

    @Test
    public void testHasMultiple() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Accept-Language", "en");
        map.put("Accept-Encoding", "gz");
        map.put("Accept-Charset", "UTF-8");
        vary = new Vary(map);
        Assert.assertFalse("Header names added", vary.isEmpty());
        Assert.assertEquals("Header names added", 3, vary.size());
    }

    @Test
    public void testDoesNotMatchRequest() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Accept-Language", "en");
        map.put("Accept-Encoding", "gz");
        map.put("Accept-Charset", "UTF-8");
        vary = new Vary(map);
        Assert.assertFalse("Header names added", vary.isEmpty());
        Assert.assertEquals("Header names added", 3, vary.size());
        HTTPRequest request = new HTTPRequest(URI.create("no.uri"));
        Assert.assertFalse(vary.matches(request));
    }

    @Test
    public void testDoesMatchesRequest() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Accept-Language", "en");
        map.put("Accept-Encoding", "gz");
        map.put("Accept-Charset", "UTF-8");
        vary = new Vary(map);
        Assert.assertFalse("Header names added", vary.isEmpty());
        Assert.assertEquals("Header names added", 3, vary.size());

        HTTPRequest request = new HTTPRequest(URI.create("no.uri"));
        for (Map.Entry<String, String> header : map.entrySet()) {
            request = request.addHeader(header.getKey(), header.getValue());
        }
        Assert.assertTrue(vary.matches(request));
    }

    @Test
    public void testAlmostMatchesRequest() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Accept-Language", "en");
        map.put("Accept-Encoding", "gz");
        map.put("Accept-Charset", "UTF-8");
        vary = new Vary(map);
        Assert.assertFalse("Header names added", vary.isEmpty());
        Assert.assertEquals("Header names added", 3, vary.size());
        HTTPRequest request = new HTTPRequest(URI.create("no.uri"));
        request = request.addHeader("Accept-Language", "de");
        request = request.addHeader("Accept-Encoding", "gz");
        request = request.addHeader("Accept-Charset", "UTF-8");

        Assert.assertFalse(vary.matches(request));
    }

    @Test
    public void acceptShouldMatch() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Accept", "application/xml");
        vary = new Vary(map);
        Assert.assertFalse("Header names added", vary.isEmpty());
        Assert.assertEquals("Header names added", 1, vary.size());
        HTTPRequest request = new HTTPRequest(URI.create("no.uri"));
        request = request.addHeader("Accept", "application/xml");
        request = request.headers(new Headers().addAccept(MIMEType.valueOf("application/xml")));
        Assert.assertTrue("request did not specify application/xml", vary.matches(request));
    }

    @Test
    public void acceptWithManyPreferencesShouldMatch() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Accept", "application/xml, application/json");
        vary = new Vary(map);
        Assert.assertFalse("Header names added", vary.isEmpty());
        Assert.assertEquals("Header names added", 1, vary.size());
        HTTPRequest request = new HTTPRequest(URI.create("no.uri"));
        request = request.addHeader("Accept", "application/xml");
        request = request.headers(new Headers().addAccept(MIMEType.valueOf("application/xml"), MIMEType.valueOf("application/json")));
        Assert.assertTrue("request did not specify application/xml", vary.matches(request));
    }

    @Test
    public void testVaryFileResolve() {
        FileManager resolver = new FileManager(TestUtil.getTestFile("target"));
        File file = resolver.resolve(Key.create(URI.create("foo"), new Vary()));
        File file2 = resolver.resolve(Key.create(URI.create("foo"), new Vary()));
        File file3 = resolver.resolve(Key.create(URI.create("foo"), new Vary(new HashMap<String, String>())));
        File file4 = resolver.resolve(Key.create(URI.create("foo"), new Vary(Collections.singletonMap("Accept-Language", "en"))));
        Assert.assertEquals(file.getAbsolutePath(), file2.getAbsolutePath());
        Assert.assertEquals(file.getAbsolutePath(), file3.getAbsolutePath());
        Assert.assertEquals(file2.getAbsolutePath(), file3.getAbsolutePath());
        Assert.assertEquals(file2.getAbsolutePath(), file3.getAbsolutePath());
        Assert.assertFalse(file4.getAbsolutePath().equals(file3.getAbsolutePath()));
    }
}
