/*
 * Copyright (c) 2010. The Codehaus. All Rights Reserved.
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

import java.io.Serializable;
import java.util.Locale;

/**
 *
 * A case insensitive key.
 *
 * Compares the wrapped string case-insensitively and hashCode is the lowercase English value.
 * This class is serializable.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public final class CaseInsensitiveKey implements Serializable {
    private static final long serialVersionUID = 429640405363982150L;
    private final String delegate;

    public CaseInsensitiveKey(final String string) {
        this.delegate = string;
    }

    public String getDelegate() {
        return delegate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CaseInsensitiveKey name1 = (CaseInsensitiveKey) o;

        return !(delegate != null ? !delegate.equalsIgnoreCase(name1.delegate) : name1.delegate != null);
    }

    @Override
    public int hashCode() {
        return delegate != null ? delegate.toLowerCase(Locale.ENGLISH).hashCode() : 0;
    }

    @Override
    public String toString() {
        return delegate;
    }
}
