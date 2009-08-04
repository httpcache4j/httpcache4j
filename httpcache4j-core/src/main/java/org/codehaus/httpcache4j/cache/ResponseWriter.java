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

import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Experimental for debugging: do not use.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class ResponseWriter {
    private final HTTPResponse response;

    public ResponseWriter(HTTPResponse response) {
        this.response = response;
    }

    public void write(Writer output) {
        PrintWriter writer = new PrintWriter(output);
        writeStatus(writer, response.getStatus());
        writeHeaders(writer, response.getHeaders());
        writer.println();
        if (response.hasPayload() && response.getPayload().isAvailable()) {
            writeBody(writer, response.getPayload());
        }
        writer.print("\r\n");
        writer.flush();
    }

    private void writeStatus(PrintWriter writer, Status status) {
        writer.println(String.format("HTTP/1.1 %s %s", status.getCode(), status.getName()));
    }

    private void writeHeaders(PrintWriter writer, Headers headers) {
        for (Header head : headers) {
            writer.println(head.toString());
        }
    }

    private void writeBody(PrintWriter writer, Payload payload) {
        InputStream stream = payload.getInputStream();
        try {
            IOUtils.copy(stream, writer);
        }
        catch (IOException e) {
            throw new HTTPException("Unable to write the body of the response", e);
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }
}
