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
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HeaderConstants;

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


    private Headers merge(final Headers base, final Headers toMerge) {
        Map<String, List<Header>> map = new HashMap<String, List<Header>>(base.getHeadersAsMap());
        map.putAll(toMerge.getHeadersAsMap());
        if (map.isEmpty()) {
            return new Headers();
        }
        return new Headers(map);
    }
    

    protected PayloadCreator getPayloadCreator() {
        return payloadCreator;
    }

    protected Headers resolveHeaders(HTTPRequest request) {
        Headers requestHeaders = request.getHeaders();
        Headers conditionalHeaders = request.getConditionals().toHeaders();
        Headers preferencesHeaders = request.getPreferences().toHeaders();

        requestHeaders = merge(merge(requestHeaders, conditionalHeaders), preferencesHeaders);
        if (!requestHeaders.hasHeader(HeaderConstants.CONTENT_TYPE) && request.hasPayload()) {
            requestHeaders.add(HeaderConstants.CONTENT_TYPE, request.getPayload().getMimeType().toString());
        }
        
        //We don't want to add headers more than once.
        return requestHeaders;
    }
}
