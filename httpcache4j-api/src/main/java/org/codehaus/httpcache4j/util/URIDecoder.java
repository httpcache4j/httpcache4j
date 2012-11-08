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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Wraps the {@link java.net.URLDecoder} of the JDK to provide a more useful interface
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class URIDecoder {
    public static String decodeUTF8(String input) {
        return decode(input, "UTF-8");
    }

    public static String decode(String input, String encoding) {
        if (input == null) {
            return null;
        }
        try {
            return URLDecoder.decode(input, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedCharsetException(encoding);
        }
    }
}
