package org.codehaus.httpcache4j.cache;

import java.util.Properties;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.Status;
import org.codehaus.httpcache4j.util.SerializationUtils;
import org.junit.Assert;
import org.junit.Test;

public class SerializableCacheItemTest {

    @Test
    public void makeSureWeCanDeserializeOurSelf() {
        SerializableCacheItem clone = (SerializableCacheItem) SerializationUtils.clone(
                new SerializableCacheItem(new DefaultCacheItem(new HTTPResponse(Status.NOT_MODIFIED, new Headers())))
        );
        Assert.assertNotNull(clone);
    }

    @Test
    public void makeSureWeCanDeserializeOurSelfABitMoreComplex() {
        SerializableCacheItem clone = (SerializableCacheItem) SerializationUtils.clone(
                new SerializableCacheItem(new DefaultCacheItem(new HTTPResponse(Status.NOT_MODIFIED, new Headers().add("Foo", "bar").add("Bar", "foo").add("FOO", "kgld"))))
        );
        Assert.assertNotNull(clone);
    }

    @Test
    public void paseCacheItemWithoutContentType() {
    	Properties properties = new Properties();
    	properties.setProperty("cache-time", "Mon, 12 May 2014 20:06:46 GMT");
    	CacheItem item = SerializableCacheItem.parse(properties);
        Assert.assertNotNull(item);
    }     

}
