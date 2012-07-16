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

package org.codehaus.httpcache4j.util;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HeaderUtils;
import org.codehaus.httpcache4j.Headers;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.PrintStream;

/**
 * Experimental for debugging: do not use.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class RequestWriter extends AbstractHTTPWriter {

    public void write(HTTPRequest request) {
        write(System.out, request);
    }

    public void write(PrintStream stream, HTTPRequest request) {
        writeRequestLine(stream, request);

        Headers all = request.getAllHeaders();
        all = all.add(HeaderUtils.toHttpDate("Date", new DateTime(DateTimeZone.forID("UTC"))));
        all = all.add("Connection", "close");
        writeHeaders(stream, all);
        if (request.hasPayload()) {
            writeBody(stream, request.getPayload());
        }
        stream.print("\r\n");
        stream.flush();
    }

    private void writeRequestLine(PrintStream writer, HTTPRequest request) {
        writer.print(String.format("%s %s HTTP/1.1\r\n", request.getMethod().toString(), request.getRequestURI().getPath()));
    }
}
