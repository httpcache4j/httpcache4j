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

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HeaderUtils;
import org.codehaus.httpcache4j.Header;
import org.codehaus.httpcache4j.HTTPException;
import org.codehaus.httpcache4j.payload.Payload;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.apache.commons.io.IOUtils;

import java.io.Writer;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Experimental for debugging: do not use.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class RequestWriter {
    private final HTTPRequest request;

    public RequestWriter(HTTPRequest request) {
        this.request = request;
    }

    public void write(Writer target) {
        PrintWriter writer = new PrintWriter(target);
        writeRequestLine(writer);
        writeGeneralHeaders(writer);
        writerRequestHeaders(writer);
        if (request.hasPayload()) {
            writeBody(writer, request.getPayload());
        }
        writer.print("\r\n");
        writer.flush();
    }

    private void writeRequestLine(PrintWriter writer) {
        writer.println(String.format("%s %s HTTP/1.1", request.getMethod().name(), request.getRequestURI()));
    }

    private void writeGeneralHeaders(PrintWriter writer) {
        Header dateHeader = HeaderUtils.toHttpDate("Date", new DateTime(DateTimeZone.forID("UTC")));
        writer.println(dateHeader);
        writer.println("Connection: close");
    }

    private void writerRequestHeaders(PrintWriter writer) {
        for (Map.Entry<String, List<Header>> entry : request.getAllHeaders()) {
            for (Header head : entry.getValue()) {
                writer.println(head);
            }
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
