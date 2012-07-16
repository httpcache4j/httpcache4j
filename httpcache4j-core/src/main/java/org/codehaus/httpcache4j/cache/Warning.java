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
import org.codehaus.httpcache4j.Header;
import org.codehaus.httpcache4j.HeaderConstants;

/**
 * The warning header:
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
class Warning {
     /**
     * MUST be included whenever the returned response is stale
     */
    public static Warning STALE_WARNING = new Warning(110, "Response is stale");

    /**
     * MUST be included if a cache returns a stale response because an attempt to revalidate the response failed, due to an inability to reach the server.
     */
    public static Warning REVALIDATE_FAILED_WARNING = new Warning(111, "Revalidation failed");

    /**
     * SHOULD be included if the cache is intentionally disconnected from the rest of the network for a period of time.
     */
    public static Warning DISCONNECT_OPERATION_WARNING = new Warning(112, "Disconnected operation");

    /**
     * MUST be included if the cache heuristically chose a freshness lifetime greater than 24 hours and the response's age is greater than 24 hours.
     */
    public static Warning HEURISTIC_EXPIRATION_WARNING = new Warning(110, "Heuristic expiration");

    /**
     * The warning text MAY include arbitrary information to be presented to a human user, or logged.
     * A system receiving this warning MUST NOT take any automated action, besides presenting the warning to the user.
     */
    public static Warning MISC_WARNING = new Warning(199, "Miscellaneous warning");

    /**
     * MUST be added by an intermediate cache or proxy if it applies any transformation changing the content-coding
     * (as specified in the Content-Encoding header) or
     * media-type (as specified in the Content-Type header) of the response,
     * or the entity-body of the response, unless this Warning code already appears in the response
     */
    public static Warning TRANSFORMATION_APPLIED_WARNING = new Warning(214, "Transformation applied");

    /**
     * The warning text MAY include arbitrary information to be presented to a human user, or logged.
     * A system receiving this warning MUST NOT take any automated action
     */
    public static Warning MISC_PERSISTENT_WARNING = new Warning(299, "Miscellaneous persistent warning");


    private final int code;
    private final String description;

    public Warning(int code, String description) {
        Preconditions.checkArgument(code >= 110 && code < 300, "The code must be between 110 and 300");
        this.code = code;
        this.description = Preconditions.checkNotNull(description, "Description may not be empty");
    }

    public Header toHeader() {
        return new Header(HeaderConstants.WARNING, String.format("%s %s %s", code, "HTTPCache4j", description));
    }


}
