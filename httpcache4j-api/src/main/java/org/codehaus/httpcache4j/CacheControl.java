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

import java.util.*;

/**
 * Experimental: do not use!
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class CacheControl {
    private int maxAge = -1;
    private int maxStale = -1;
    private int minFresh = -1;
    //TODO: what semantic meaning does this have over max-age? I think it overrides
    private int sMaxAge = -1; //http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.3 //ignored for now
    private Set<Directive> directives = new HashSet<Directive>();

    public CacheControl(Map<Directive, String> directives) {
        this.directives.addAll(directives.keySet());
        parseDirectiveMap(directives);
    }

    public CacheControl(Header header) {
        parse(header);
    }

    public static CacheControl valueOf(String headerValue) {
        return new CacheControl(new Header(HeaderConstants.CACHE_CONTROL, headerValue));
    }

    private void parse(Header header) {
        Map<Directive, String> directiveMap = convertDirectiveMap(header);
        parseDirectiveMap(directiveMap);
        directives.addAll(directiveMap.keySet());
        if (directives.isEmpty()) {
            throw new IllegalArgumentException("Unkown value from header: " + header.getValue());
        }
    }

    private void parseDirectiveMap(Map<Directive, String> directiveMap) {
        String directiveValue = directiveMap.get(Directive.MAX_AGE);
        maxAge = NumberUtils.toInt(directiveValue, -1);
        directiveValue = directiveMap.get(Directive.MAX_STALE);
        maxStale = NumberUtils.toInt(directiveValue, -1);
        directiveValue = directiveMap.get(Directive.MIN_FRESH);
        minFresh = NumberUtils.toInt(directiveValue, -1);
        directiveValue = directiveMap.get(Directive.S_MAX_AGE);
        sMaxAge = NumberUtils.toInt(directiveValue, -1);
    }

    private Map<Directive, String> convertDirectiveMap(Header header) {
        Map<Directive, String> directives = new HashMap<Directive, String>();
        for (Directive directive : Directive.values()) {
            directives.put(directive, header.getDirectives().get(directive.getValue()));
        }
        return directives;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public int getMaxStale() {
        return maxStale;
    }

    public int getMinFresh() {
        return minFresh;
    }

    int getSMaxAge() {
        return sMaxAge;
    }

    public boolean isPrivate() {
        return directives.contains(Directive.PRIVATE);
    }

    public Header toHeader() {
        return null;
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
