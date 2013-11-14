package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.util.LRUMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class LRUMapTest {

    private LRUMap<String,Integer> map;

    @Before
    public void setUp() throws Exception {
        map = new LRUMap<String, Integer>(2);
    }

    @Test
    public void capacity() {

        map.put("hello", 1);
        map.put("hello2", 2);
        assertThat(map.size(), equalTo(2));
        map.put("hello3", 2);
        assertThat(map.size(), equalTo(2));
    }

    @Test
    public void putListeners() {
        final AtomicInteger counter = new AtomicInteger();
        map.addListener(new LRUMap.ModificationListener<String, Integer>() {
            @Override
            public void onPut(String key, Integer value) {
                counter.incrementAndGet();
            }

            @Override
            public void onRemove(String key, Integer value) {

            }
        });
        map.put("hello", 1);
        map.put("hello2", 2);
        assertThat(counter.get(), equalTo(2));
    }

    @Test
    public void putAndRemoveListeners() {
        final AtomicInteger putCounter = new AtomicInteger();
        final AtomicInteger removeCounter = new AtomicInteger();
        map.addListener(new LRUMap.ModificationListener<String, Integer>() {
            @Override
            public void onPut(String key, Integer value) {
                putCounter.incrementAndGet();
            }

            @Override
            public void onRemove(String key, Integer value) {
                removeCounter.incrementAndGet();
            }
        });
        map.put("hello", 1);
        map.put("hello2", 2);
        map.put("hello3", 3);
        assertThat(putCounter.get(), equalTo(3));
        map.remove("hello2");
        assertThat(removeCounter.get(), equalTo(2));
    }

    @Test
    public void removeListenersNotCalledWhenRemovingNoneExistingElement() {
        final AtomicInteger removeCounter = new AtomicInteger();
        map.addListener(new LRUMap.ModificationListener<String, Integer>() {
            @Override
            public void onPut(String key, Integer value) {
                throw new UnsupportedOperationException("Do not call this");
            }

            @Override
            public void onRemove(String key, Integer value) {
                removeCounter.incrementAndGet();
            }
        });
        map.remove("hello");
        assertThat(removeCounter.get(), equalTo(0));
    }

    @After
    public void tearDown() throws Exception {
        map.removeListeners();
    }
}
