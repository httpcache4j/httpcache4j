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

import com.google.common.base.Preconditions;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.codehaus.httpcache4j.HeaderConstants.VARY;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class Key implements Serializable {
    private static final long serialVersionUID = 5827064595759738979L;

    private URI uri;
    private Vary vary;

    public static Key create(URI uri, Vary vary) {
        Preconditions.checkNotNull(uri, "URI may not be null");
        Preconditions.checkNotNull(vary, "vary may not be null");
        return new Key(uri, vary);
    }

    public static Key create(HTTPRequest request, HTTPResponse response) {
        URI uri = request.getRequestURI();
        return new Key(uri, determineVariation(response.getHeaders(), request.getAllHeaders()));
    }

    private static Vary determineVariation(Headers responseHeaders, Headers requestHeaders) {
        String varyHeader = responseHeaders.getFirstHeaderValue(VARY);
        Map<String, String> resolvedVaryHeaders = new HashMap<String, String>();
        if (varyHeader != null) {
            String[] varies = varyHeader.split(",");
            for (String vary : varies) {
                String value = requestHeaders.getFirstHeaderValue(vary);
                if (value != null) {
                    resolvedVaryHeaders.put(vary, value);
                }
            }
        }
        return new Vary(resolvedVaryHeaders);
    }


    Key(URI uri, Vary vary) {
        Preconditions.checkNotNull(uri, "URI may not be null");
        Preconditions.checkNotNull(vary, "Vary may not be null");
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
       return toProperties().toString();
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
        out.writeObject(toProperties());
    }


    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        Properties properties = (Properties) in.readObject();
        Key key = parse(properties);
        uri = key.getURI();
        vary = key.getVary();
    }

    public static Key parse(Properties properties) {
        URI uri = null;
        Vary vary = null;
        if (properties.containsKey("uri")) {
            uri = URI.create(properties.getProperty("uri"));
        }
        if (properties.containsKey("vary")) {
            vary = Vary.parse(properties.getProperty("vary"));
        }
        return new Key(uri, vary);
    }

    public Properties toProperties() {
        Properties object = new Properties();
        object.put("uri", uri.toString());
        object.put("vary", vary.toString());
        return object;
    }
}
