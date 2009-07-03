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

package org.codehaus.httpcache4j.util;

import java.util.Map;
import java.io.Serializable;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 * A simple Map.Entry implementaton.
 */
public class Pair<K, V> implements Map.Entry<K, V>, Serializable {
    K key;
    V value;
    private static final long serialVersionUID = -4941247633392417930L;

    Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public static <K, V> Pair<K,V> of(K key, V value) {
        return new Pair<K,V>(key, value);
    }

    public V getValue() {
        return value;
    }

    public V setValue(V pValue) {
        throw new UnsupportedOperationException("Not allowed to a value here.");
    }

    public K getKey() {
        return key;
    }

    public boolean equals(Object pOther) {
        if (!(pOther instanceof Map.Entry)) {
            return false;
        }

        Map.Entry entry = (Map.Entry) pOther;

        Object k1 = key;
        Object k2 = entry.getKey();

        if (k1 == k2 || (k1 != null && k1.equals(k2))) {
            Object v1 = value;
            Object v2 = entry.getValue();

            if (v1 == v2 || (v1 != null && v1.equals(v2))) {
                return true;
            }
        }

        return false;
    }

    public int hashCode() {
        return 31 * (key == null ? 0 : key.hashCode()) ^
                (value == null ? 0 : value.hashCode());
    }

    public String toString() {
        return getKey() + "=" + getValue();
    }
}
