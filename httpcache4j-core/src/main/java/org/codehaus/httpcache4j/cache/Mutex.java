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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @author <a href="mailto:erik@mogensoft.net">Erik Mogensen</p>
 * @version $Revision: $
 */
class Mutex<T> {
    private final Set<T> locks = new CopyOnWriteArraySet<T>();
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private final Logger logger = Logger.getLogger(getClass().getName());

    public boolean acquire(T object) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(String.format("About to acquire lock for %s", object));
        }
        lock.lock();
        try {
            while (locks.contains(object)) {
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.warning(String.format("Thread trying to get lock for %s was interrupted", object));
                    }
                    Thread.currentThread().interrupt();
                    locks.remove(object);
                    return false;
                }
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(String.format("Adding %s to locks", object));
            }
            locks.add(object);
        }
        finally {
            lock.unlock();
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(String.format("Acquired lock for %s", object));
        }
        return true;
    }

    public void release(T object) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(String.format("About to release lock for %s", object));
        }
        lock.lock();
        try {
            if (locks.remove(object)) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(String.format("Removing %s from locks", object));
                }
                condition.signalAll();
            }

        } finally {
            lock.unlock();
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(String.format("Released lock for %s", object));
        }
    }
}
