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

import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Headers;
import static org.codehaus.httpcache4j.HeaderConstants.VARY;

import java.net.URI;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class Key implements Serializable {
    private static final long serialVersionUID = 5827064595759738979L;
    
    private URI uri;
    private Vary vary;

    public static Key create(URI uri, Vary vary) {
        return new Key(uri, vary);
    }
    public static Key create(HTTPRequest request, HTTPResponse response) {
        URI uri = request.getRequestURI();
        return new Key(uri, determineVariation(request.getAllHeaders(), response.getHeaders()));
    }

    private static Vary determineVariation(Headers responseHeaders, Headers requestHeaders) {
        String varyHeader = responseHeaders.getFirstHeaderValue(VARY);
        Map<String, String> resolvedVaryHeaders = new HashMap<String, String>();
        if (varyHeader != null) {
            String[] varies = varyHeader.split(",");
            for (String vary : varies) {
                String value = requestHeaders.getFirstHeaderValue(vary);
                resolvedVaryHeaders.put(vary, value == null ? null : value);
            }
        }
        return new Vary(resolvedVaryHeaders);
    }


    Key(URI uri, Vary vary) {
        Validate.notNull(uri, "URI may not be null");
        Validate.notNull(vary, "Vary may not be null");
        this.uri = uri;
        this.vary = vary;
    }

    public URI getURI() {
        return uri;
    }

    public Vary getVary() {
        return vary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Key key = (Key) o;

        if (uri != null ? !uri.equals(key.uri) : key.uri != null) {
            return false;
        }
        if (vary != null ? !vary.equals(key.vary) : key.vary != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (vary != null ? vary.hashCode() : 0);
        return result;
    }
}
