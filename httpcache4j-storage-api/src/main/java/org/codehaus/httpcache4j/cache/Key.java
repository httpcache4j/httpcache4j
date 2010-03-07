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
import org.codehaus.httpcache4j.util.ToJSON;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.codehaus.httpcache4j.HeaderConstants.VARY;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class Key implements Serializable, ToJSON {
    private static final long serialVersionUID = 5827064595759738979L;

    private URI uri;
    private Vary vary;

    public static Key create(URI uri, Vary vary) {
        Validate.notNull(uri, "URI may not be null");
        Validate.notNull(vary, "vary may not be null");
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
    public String toString() {
       return toJSON();
    }


    @Override
    public String toJSON() {
        Map<String, String> object = new LinkedHashMap<String, String>();
        object.put("uri", uri.toString());
        object.put("vary", vary.toJSON());
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
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


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(toJSON());
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        String jsonValue = (String) in.readObject();
        Key key = fromJSON(jsonValue);
        uri = key.getURI();
        vary = key.getVary();
    }

    private Key fromJSON(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(json);
            URI uri = URI.create(node.path("uri").getValueAsText());
            Vary vary = Vary.fromJSON(node.path("vary").getValueAsText());
            return new Key(uri, vary);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
