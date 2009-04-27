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
package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.resolver.AbstractPayloadCreator;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.*;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

/**
 * An implementation of Payload creator that creates ByteArrayPayloads. This is mostly useful for testing.
 * <p/>

 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class InMemoryPayloadCreator extends AbstractPayloadCreator {
    
    protected Payload createCachedPayload(URI requestURI, InputStream stream, MIMEType type) throws IOException {
        return new ByteArrayPayload(stream, type);
    }
    
    private class ByteArrayPayload implements Payload, Serializable {
        private byte[] bytes;
        private MIMEType type;

        private ByteArrayPayload(InputStream stream, MIMEType type) throws IOException {
            this.bytes = IOUtils.toByteArray(stream);
            this.type = type;
        }

        public MIMEType getMimeType() {
            return type;
        }

        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        public boolean isAvailable() {
            return bytes != null;
        }

        public boolean isTransient() {
            return false;
        }
    }
}
