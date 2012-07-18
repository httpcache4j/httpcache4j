package org.codehaus.httpcache4j;


import org.codehaus.httpcache4j.util.DirectivesParser;
import org.codehaus.httpcache4j.util.NumberUtils;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class CacheControl {
    private final Directives directives;

    public CacheControl(String value) {
        this(DirectivesParser.parse(value));
    }

    public CacheControl(Header header) {
        if (!HeaderConstants.CACHE_CONTROL.equalsIgnoreCase(header.getName())) {
            throw new IllegalArgumentException("Not a Cache-Control header");
        }
        this.directives = header.getDirectives();
    }

    public CacheControl(final Directives directives) {
        this.directives = directives;
    }

    public boolean isPrivate() {
        return directives.hasDirective("private");
    }

    public int getMaxAge() {
        return NumberUtils.toInt(directives.get("max-age"), -1);
    }

    public int getSMaxAge() {
        return NumberUtils.toInt(directives.get("s-maxage"), -1);
    }

    public boolean isPublic() {
        return directives.hasDirective("public");
    }

    public int getMaxStale() {
        return NumberUtils.toInt(directives.get("max-stale"), -1);
    }

    public int getMinFresh() {
        return NumberUtils.toInt(directives.get("min-fresh"), -1);
    }

    public boolean isNoTransform() {
        return directives.hasDirective("no-transform");
    }

    public boolean isMustRevalidate() {
        return directives.hasDirective("must-revalidate");
    }

    public boolean isProxyRevalidate() {
        return directives.hasDirective("proxy-revalidate");
    }

    public boolean isNoStore() {
        return directives.hasDirective("no-store");
    }

    public boolean isNoCache() {
        return directives.hasDirective("no-cache");
    }

    public Directives getDirectives() {
        return directives;
    }

    public Header toHeader() {
        return new Header(HeaderConstants.CACHE_CONTROL, new Directives(directives));
    }
}
