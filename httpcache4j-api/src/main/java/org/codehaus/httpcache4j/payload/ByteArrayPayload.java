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

package org.codehaus.httpcache4j.payload;

import org.codehaus.httpcache4j.MIMEType;
import org.apache.commons.io.IOUtils;

import java.io.Serializable;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
* @version $Revision: $
*/
public class ByteArrayPayload implements Payload, Serializable {
    private static final long serialVersionUID = -4845254892809632007L;
    private byte[] bytes;
    private MIMEType type;

    public ByteArrayPayload(InputStream stream, MIMEType type) throws IOException {
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
