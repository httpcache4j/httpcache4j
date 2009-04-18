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

package org.codehaus.httpcache4j;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class CacheControl {
    private int maxAge;
    private Directive directive;

    public CacheControl(Header header) {
        if (!HeaderConstants.CACHE_CONTROL.equalsIgnoreCase(header.getName())) {
            throw new IllegalArgumentException("Trying to construct a CacheControl object without a CacheControl Header");
        }
        parse(header);
    }

    private void parse(Header header) {
        for (Directive directive : Directive.values()) {
            if  (directive.getValue().equalsIgnoreCase(header.getValue())) {
                this.directive = directive;
            }
        }
        if (this.directive == null) {
            throw new IllegalArgumentException("Unkown value from header: " + header.getValue());
        }
        else {
            if (directive == Directive.PRIVATE || directive == Directive.PUBLIC) {
                String maxAgeString = header.getDirectives().get(Directive.MAX_AGE.getValue());
                if (StringUtils.isEmpty(maxAgeString)) {
                    maxAge = NumberUtils.toInt(maxAgeString);
                }
            }
        }
    }


    public enum Directive {
        PRIVATE("private"),
        PUBLIC("public"),
        MUST_REVALIDATE("must-revalidate"),
        NO_STORE("no-store"),
        NO_CACHE("no-cache"),
        NO_TRANSFORM("no-transform"),
        MAX_AGE("max-age"),
        MAX_STALE("max-stale"),
        ONLY_IF_CACHED("only-if-cached"),
        MIN_FRESH("min-fresh"),
        S_MAX_AGE("s-maxage"),
        PROXY_REVALIDATE("proxy-revalidate");

        private final String value;

        private Directive(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
