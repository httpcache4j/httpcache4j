package org.codehaus.httpcache4j.util;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import org.codehaus.httpcache4j.HTTPException;
import org.codehaus.httpcache4j.Header;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.payload.Payload;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public abstract class AbstractHTTPWriter {

    protected void writeHeaders(PrintStream writer, Headers headers) {
        StringBuilder builder = new StringBuilder();
        for (Header header : headers) {
            if (builder.length() > 0) {
                builder.append("\r\n");
            }
            builder.append(header);
        }
        println(writer, builder.toString());
    }

    protected void println(PrintStream writer, String value) {
        writer.printf("%s\r\n", value);
    }

    protected void writeBody(PrintStream writer, Payload payload) {
        writer.print("\r\n");
        InputStream stream = payload.getInputStream();
        try {
            ByteStreams.copy(stream, writer);
            writer.print("\r\n");
        }
        catch (IOException e) {
            throw new HTTPException("Unable to write the body of the response", e);
        }
        finally {
            Closeables.closeQuietly(stream);
        }
    }

}
