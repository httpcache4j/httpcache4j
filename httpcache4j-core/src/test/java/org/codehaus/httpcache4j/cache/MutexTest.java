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

import org.junit.Test;
import org.junit.Assert;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.net.URI;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class MutexTest {
    private final Mutex<URI> mutex = new Mutex<URI>();
    private static final URI URI_1 = URI.create("foo");
    public AtomicBoolean flag = new AtomicBoolean(false);
    private Map<String, Boolean> success = new ConcurrentHashMap<String, Boolean>();

    @Test
    public void test() throws Exception {
        testWithRunnables(new DoTheThing());
    }

    @Test
    public void test10Times() throws Exception {
        for (int i = 0; i < 10; i++) {
            test();
        }
    }

    @Test
    public void testRandomExceptionThrowing() throws Exception {
        final Random random = new Random();
        testWithRunnables(new Runnable() {
            public void run() {
                mutex.acquire(URI_1);
                try {
                    if (flag.get()) {
                        success.put(Thread.currentThread().getName(), true);
                    } else {
                        success.put(Thread.currentThread().getName(), false);
                    }

                    if (random.nextInt() % 2 == 0) {
                        throw new RuntimeException();
                    }
                }
                catch (RuntimeException e) {
                    if (flag.get()) {
                        success.put(Thread.currentThread().getName(), true);
                    } else {
                        success.put(Thread.currentThread().getName(), false);
                    }
                    throw e;
                } finally {
                    mutex.release(URI_1);
                }
            }
        });
    }

    private void testWithRunnables(final Runnable worker) throws InterruptedException {
        List<Thread> thread = new ArrayList<Thread>();
        final DoTheThingSlowly runnable = new DoTheThingSlowly();
        Thread slowThread = new Thread(runnable);
        slowThread.start();
        synchronized (runnable) {
            runnable.wait();
        }
        thread.add(slowThread);
        for (int i = 0; i < 4; i++) {
            Thread t = new Thread(worker);
            t.start();
            thread.add(t);
        }

        for (Thread t : thread) {
            t.join();
        }

        for (Map.Entry<String, Boolean> isSuccess : success.entrySet()) {
            Assert.assertTrue(isSuccess.getKey() + " did not complete successfully", isSuccess.getValue());
        }
    }

    class DoTheThing implements Runnable {
        public void run() {
            mutex.acquire(URI_1);
            try {
                if (flag.get()) {
                    success.put(Thread.currentThread().getName(), true);
                } else {
                    success.put(Thread.currentThread().getName(), false);
                }
            } finally {
                mutex.release(URI_1);
            }
        }
    }

    class DoTheThingSlowly implements Runnable {
        public void run() {
            mutex.acquire(URI_1);
            synchronized (this) {
                notify();                
            }
            try {
                Thread.sleep(1000);
                flag.set(true);
            } catch (InterruptedException e) {
                success.put(Thread.currentThread().getName(), false);
                Thread.currentThread().interrupt();
            } finally {
                mutex.release(URI_1);
            }
        }
    }
}



