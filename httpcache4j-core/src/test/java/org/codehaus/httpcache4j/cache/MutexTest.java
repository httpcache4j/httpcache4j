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
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class MutexTest {
    private final Mutex<URI> mutex = new Mutex<URI>();
    private static final URI URI_1 = URI.create("foo");
    public AtomicBoolean flag = new AtomicBoolean(false);
    private Map<String, Boolean> success = new ConcurrentHashMap<String, Boolean>();

    @Test
    public void test() throws Exception {
        List<Thread> threads = testWithRunnables(new DoTheThing(URI_1), URI_1);
        joinAndAssert(threads);
    }

    @Test
    public void test10Times() throws Exception {
        for (int i = 0; i < 10; i++) {
            test();
        }
    }

    @Test
    public void makeSureThatDifferentURIDoesNotBlockOtherThreads() throws Exception {
        List<Thread> threads = testWithRunnables(new Runnable() {
            public void run() {
                mutex.acquire(URI.create("bar"));
                try {
                    success.put(Thread.currentThread().getName(), true);
                } finally {
                    mutex.release(URI.create("bar"));
                }
            }
        }, URI_1);

        joinAndAssert(threads);
    }

    @Test
    public void testRandomExceptionThrowing() throws Exception {
        final Random random = new Random();
        Runnable worker = new Runnable() {
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
        };
        List<Thread> threads = testWithRunnables(worker, URI_1);
        joinAndAssert(threads);

    }

    @Test
    public void verifyHTCJ101() throws Exception {
        final Mutex<String> mutex = new Mutex<String>();
        final String resourceToLock = "some-resourceID";

        final Runnable t1 = new Runnable() {
            public void run() {
                boolean acq = mutex.acquire(resourceToLock);

                // Give the T2 time to try to acquire the lock
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignore) {
                }
                Assert.assertTrue("Failed to lock resource",acq);
                mutex.release(resourceToLock);
            }
        };
        Runnable t2 = new Runnable() {
            public void run() {
                System.out.println("Started");
                // Delay to make sure that T1 actually starts before T2
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                }
                // Simulate that another thread tries to interrupt T2 right now
                Thread.currentThread().interrupt();
                Assert.assertFalse(mutex.acquire(resourceToLock));
            }
        };

        Thread thread1 = new Thread(t1, "T1");
        thread1.start();
        
        Thread thread2 = new Thread(t2, "T2");
        thread2.start();
        thread1.join();
        thread2.join();
    }

    private List<Thread> testWithRunnables(final Runnable worker, URI uri) throws InterruptedException {
        List<Thread> threads = new ArrayList<Thread>();
        final DoTheThingSlowly runnable = new DoTheThingSlowly(uri);
        Thread slowThread = new Thread(runnable);
        slowThread.start();
        synchronized (runnable) {
            runnable.wait();
        }
        threads.add(slowThread);
        for (int i = 0; i < 4; i++) {
            Thread t = new Thread(worker);
            t.start();
            threads.add(t);
        }
        return threads;
    }

    private void joinAndAssert(List<Thread> thread) throws InterruptedException {
        for (Thread t : thread) {
            t.join();
        }

        for (Map.Entry<String, Boolean> isSuccess : success.entrySet()) {
            Assert.assertTrue(isSuccess.getKey() + " did not complete successfully", isSuccess.getValue());
        }
    }

    class DoTheThing implements Runnable {
        private final String uri;

        public DoTheThing(URI uri) {
            this.uri = uri.toString();
        }

        public void run() {
            mutex.acquire(URI.create(uri));
            try {
                if (flag.get()) {
                    success.put(Thread.currentThread().getName() + " " + uri, true);
                } else {
                    success.put(Thread.currentThread().getName() + " " + uri, false);
                }
            } finally {
                mutex.release(URI.create(uri));
            }
        }
    }

    class DoTheThingSlowly implements Runnable {
        private final String uri;

        public DoTheThingSlowly(URI uri) {
            this.uri = uri.toString();
        }

        public void run() {
            mutex.acquire(URI.create(uri));
            synchronized (this) {
                notify();
            }
            try {
                Thread.sleep(2000);
                flag.set(true);
            } catch (InterruptedException e) {
                success.put(Thread.currentThread().getName(), false);
                Thread.currentThread().interrupt();
            } finally {
                mutex.release(URI.create(uri));
            }
        }
    }
}



