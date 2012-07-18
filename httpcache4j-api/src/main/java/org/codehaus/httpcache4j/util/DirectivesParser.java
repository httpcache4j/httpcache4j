/*
 * Copyright (c) 2010. The Codehaus. All Rights Reserved.
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

import com.google.common.base.Strings;
import org.codehaus.httpcache4j.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * Modified version of the HTTP Components BasicHeaderValueParser.
 * Original authors: http://hc.apache.org/httpcomponents-core/index.html
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public final class DirectivesParser {

    public static final int CR = 13; // <US-ASCII CR, carriage return (13)>
    public static final int LF = 10; // <US-ASCII LF, linefeed (10)>
    public static final int SP = 32; // <US-ASCII SP, space (32)>
    public static final int HT = 9;  // <US-ASCII HT, horizontal-tab (9)>


    public static boolean isWhitespace(char ch) {
        return ch == SP || ch == HT || ch == CR || ch == LF;
    }

    private final static char PARAM_DELIMITER = ';';
    private final static char ELEM_DELIMITER = ',';
    private final static char[] ALL_DELIMITERS = new char[]{
            PARAM_DELIMITER,
            ELEM_DELIMITER
    };


    public static Directives parse(String value) {
        if (value.length() > 0) {
            StringBuilder builder = new StringBuilder(value);
            ParserCursor cursor = new ParserCursor(0, value.length());
            DirectivesParser parser = new DirectivesParser();
            return new Directives(parser.parseDirectives(builder, cursor));
        }
        return new Directives();
    }

    private List<Directive> parseDirectives(final StringBuilder buffer, final ParserCursor cursor) {
        List<Directive> elements = new ArrayList<Directive>();
        while (!cursor.atEnd()) {
            Directive element = parseDirective(buffer, cursor);
            if (!(element.getName().length() == 0 && element.getValue() == null)) {
                elements.add(element);
            }
        }
        return elements;
    }

    private Directive parseDirective(final StringBuilder buffer, final ParserCursor cursor) {
        Parameter parameter = parseParameter(buffer, cursor, ALL_DELIMITERS);
        List<Parameter> params = Collections.emptyList();
        if (!cursor.atEnd()) {
            char ch = buffer.charAt(cursor.getPos() - 1);
            if (ch != ELEM_DELIMITER) {
                params = parseParameters(buffer, cursor);
            }
        }
        if (parameter instanceof QuotedParameter) {
            return createDirective(parameter.getName(), ((QuotedParameter) parameter).getQuotedValue(), params);
        }
        return createDirective(parameter.getName(), parameter.getValue(), params);
    }


    /**
     * Creates a header element.
     * Called from {@link #parseDirective}.
     *
     * @return a header element representing the argument
     */
    private Directive createDirective(
            final String name,
            final String value,
            final List<Parameter> params) {
        if (isQuoted(value)) {
            return new QuotedDirective(name, value, params);
        }
        if (HeaderConstants.LINK_HEADER.equals(name)) {
            return new LinkDirective(value, params);
        }
        return new Directive(name, value, params);
    }

    private List<Parameter> parseParameters(final StringBuilder buffer, final ParserCursor cursor) {

        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        }
        if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }

        int pos = cursor.getPos();
        int indexTo = cursor.getUpperBound();

        while (pos < indexTo) {
            char ch = buffer.charAt(pos);
            if (isWhitespace(ch)) {
                pos++;
            }
            else {
                break;
            }
        }
        cursor.updatePos(pos);
        if (cursor.atEnd()) {
            return Collections.emptyList();
        }

        List<Parameter> params = new ArrayList<Parameter>();
        while (!cursor.atEnd()) {
            params.add(parseParameter(buffer, cursor, ALL_DELIMITERS));
            char ch = buffer.charAt(cursor.getPos() - 1);
            if (ch == ELEM_DELIMITER) {
                break;
            }
        }

        return params;
    }

    private static boolean isOneOf(final char ch, final char[] chs) {
        if (chs != null) {
            for (int i = 0; i < chs.length; i++) {
                if (ch == chs[i]) {
                    return true;
                }
            }
        }
        return false;
    }

    private Parameter parseParameter(final StringBuilder buffer,
                                            final ParserCursor cursor,
                                            final char[] delimiters) {

        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        }
        if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }

        boolean terminated = false;

        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();

        // Find name
        String name = null;
        while (pos < indexTo) {
            char ch = buffer.charAt(pos);
            if (ch == '=') {
                break;
            }
            if (isOneOf(ch, delimiters)) {
                terminated = true;
                break;
            }
            pos++;
        }

        if (pos == indexTo) {
            terminated = true;
            name = buffer.substring(indexFrom, indexTo).trim();
        }
        else {
            name = buffer.substring(indexFrom, pos).trim();
            pos++;
        }

        if (terminated) {
            cursor.updatePos(pos);
            int i = name.indexOf('<');
            int j = name.indexOf('>');
            if (i != -1 && j != -1) { //this is a Link header
                return createParameter(HeaderConstants.LINK_HEADER, name.substring(i + 1, j));
            }
            
            return createParameter(name, null);
        }

        // Find value
        String value = null;
        int i1 = pos;

        boolean qouted = false;
        boolean escaped = false;
        while (pos < indexTo) {
            char ch = buffer.charAt(pos);
            if (ch == '"' && !escaped) {
                qouted = !qouted;
            }
            if (!qouted && !escaped && isOneOf(ch, delimiters)) {
                terminated = true;
                break;
            }
            if (escaped) {
                escaped = false;
            }
            else {
                escaped = qouted && ch == '\\';
            }
            pos++;
        }

        int i2 = pos;
        // Trim leading white spaces
        while (i1 < i2 && (isWhitespace(buffer.charAt(i1)))) {
            i1++;
        }
        // Trim trailing white spaces
        while ((i2 > i1) && (isWhitespace(buffer.charAt(i2 - 1)))) {
            i2--;
        }        
        value = buffer.substring(i1, i2);
        if (terminated) {
            pos++;
        }
        cursor.updatePos(pos);
        return createParameter(name, value);
    }

    /**
     * Creates a Parameter
     *
     * @param name  the name
     * @param value the value, or <code>null</code>
     * @return a name-value pair representing the arguments
     */
    static Parameter createParameter(final String name, final String value) {
        if (value != null && isQuoted(value)) {
            return new QuotedParameter(name, value);
        }
        return new Parameter(name, value);
    }

    static boolean isQuoted(String value) {
        return value.startsWith("\"") && value.endsWith("\"");
    }

}