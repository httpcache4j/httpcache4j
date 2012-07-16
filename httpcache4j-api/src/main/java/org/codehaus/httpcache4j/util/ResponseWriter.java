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

import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.StatusLine;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Experimental for debugging: do not use.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class ResponseWriter extends AbstractHTTPWriter {
    public void write(HTTPResponse response) {
        write(response, System.out);
    }

    public void write(HTTPResponse response, PrintStream stream) {
        writeStatus(stream, response.getStatusLine());
        writeHeaders(stream, response.getHeaders());
        if (response.hasPayload()) {
            writeBody(stream, response.getPayload());
        }
        stream.flush();
    }

    private void writeStatus(PrintStream writer, StatusLine status) {
        println(writer, String.format("%s %s %s", status.getStatus().getCode(), status.getMessage(), status.getVersion()));
        writer.print("\r\n");
    }
}
