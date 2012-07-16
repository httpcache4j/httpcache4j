package org.codehaus.httpcache4j.cache;

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
                new SerializableCacheItem(new DefaultCacheItem(new HTTPResponse(null, Status.NOT_MODIFIED, new Headers())))
        );
        Assert.assertNotNull(clone);
    }

    @Test
    public void makeSureWeCanDeserializeOurSelfABitMoreComplex() {
        SerializableCacheItem clone = (SerializableCacheItem) SerializationUtils.clone(
                new SerializableCacheItem(new DefaultCacheItem(new HTTPResponse(null, Status.NOT_MODIFIED, new Headers().add("Foo", "bar").add("Bar", "foo").add("FOO", "kgld"))))
        );
        Assert.assertNotNull(clone);
    }

}
