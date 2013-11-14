package org.codehaus.httpcache4j.util;

import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.cache.CacheItem;
import org.codehaus.httpcache4j.cache.Key;
import org.codehaus.httpcache4j.cache.Vary;
import org.joda.time.DateTime;
import org.junit.Test;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class MemoryCacheTest {

    @Test
    public void listenerWorksCorrectly() {
        MemoryCache cache = new MemoryCache(5);

        final AtomicInteger counter = new AtomicInteger();
        cache.setKeyListener(new MemoryCache.KeyListener() {
            @Override
            public void onRemove(Key key) {
                counter.incrementAndGet();
            }
        });

        LRUMap<Vary, CacheItem> value = new LRUMap<Vary, CacheItem>(2);
        URI uri = URI.create("foo");
        cache.put(uri, value);
        value.put(new Vary(), new TestCacheItem());

        cache.remove(Key.create(uri, new Vary()));
        assertThat(counter.get(), equalTo(1));

        value.put(new Vary(), new TestCacheItem());

        value.remove(new Vary());

        assertThat(counter.get(), equalTo(2));

        value.put(new Vary(), new TestCacheItem());
        value.put(new Vary(new Headers().addAccept(MIMEType.valueOf("application/json"))), new TestCacheItem());
        value.put(new Vary(new Headers().addAccept(MIMEType.valueOf("application/xml"))), new TestCacheItem());

        assertThat(counter.get(), equalTo(3));
        assertThat(cache.size(), equalTo(1));
        assertThat(cache.size() + value.size(), equalTo(3));
    }





    private class TestCacheItem implements CacheItem {
        @Override
        public int getTTL() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isStale(DateTime requestTime) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getAge(DateTime dateTime) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DateTime getCachedTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public HTTPResponse getResponse() {
            throw new UnsupportedOperationException();
        }
    }
}
