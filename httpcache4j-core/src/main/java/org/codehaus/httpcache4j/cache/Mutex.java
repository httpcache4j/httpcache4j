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

import com.google.common.collect.Sets;

import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @author <a href="mailto:erik@mogensoft.net">Erik Mogensen</p>
 * @version $Revision: $
 */
class Mutex<T> {
    private final Set<T> locks = Sets.newHashSet();
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public void acquire(T uri) {
        lock.lock();
        try {
            while (locks.contains(uri)) {
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            locks.add(uri);
        }
        finally {
            lock.unlock();
        }
    }

    public void release(T uri) {
        this.lock.lock();
        try {
            if (locks.contains(uri)) {
                if(locks.remove(uri)) {
                    condition.signal();
                }
            }

        } finally {
            this.lock.unlock();
        }
    }

}