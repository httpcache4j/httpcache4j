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

package org.codehaus.httpcache4j;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public final class HTTPUtil {
    private HTTPUtil() {
    }

    public static boolean isCacheableRequest(HTTPRequest request) {
        if (request.getMethod() == HTTPMethod.GET || request.getMethod() == HTTPMethod.HEAD) {
            if (request.getHeaders().hasHeader(HeaderConstants.CACHE_CONTROL)) {
                String cacheControlHeaderValue = request.getHeaders().getFirstHeader(HeaderConstants.CACHE_CONTROL).getValue();
                //If the request tells us that we shouldn't cache the response, then we don't.
                return !cacheControlHeaderValue.contains("no-store") || !cacheControlHeaderValue.contains("no-cache");
            }
            return true;
        }
        return false;
    }
}
