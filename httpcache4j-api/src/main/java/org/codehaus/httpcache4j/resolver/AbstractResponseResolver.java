/*
 * Copyright (c) 2008, The Codehaus. All Rights Reserved.
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
 *
 */

package org.codehaus.httpcache4j.resolver;

import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.Header;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Implementors should implement this instead of using the ResponseResolver interface directly.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public abstract class AbstractResponseResolver implements ResponseResolver {
    private PayloadCreator payloadCreator;

    public AbstractResponseResolver(PayloadCreator payloadCreator) {
        Validate.notNull(payloadCreator, "You may not add a null Payload creator");
        this.payloadCreator = payloadCreator;
    }


    protected Headers removePotentialDuplicates(final Headers headersToRemoveFrom, final Headers headers) {
        Map<String, List<Header>> map = new HashMap<String, List<Header>>(headersToRemoveFrom.getHeadersAsMap());
        for (String key : headers.getHeadersAsMap().keySet()) {
            if (map.containsKey(key)) {
                map.remove(key);
            }
        }
        if (map.isEmpty()) {
            return new Headers();
        }
        return new Headers(map);
    }
    

    protected PayloadCreator getPayloadCreator() {
        return payloadCreator;
    }
}
