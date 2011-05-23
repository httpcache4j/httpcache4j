package org.codehaus.httpcache4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: maedhros
 * Date: Oct 14, 2010
 * Time: 11:22:40 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class CacheHeaderBuilder {
    private final static AtomicReference<CacheHeaderBuilder> INSTANCE = new AtomicReference<CacheHeaderBuilder>(new LocalHostCacheHeaderBuilder());
    
    public abstract Header createMISSXCacheHeader();
    public abstract Header createHITXCacheHeader();
    

    public static void setBuilder(CacheHeaderBuilder builder) {
        INSTANCE.set(builder);
    }
    
    public static CacheHeaderBuilder getBuilder() {
        return INSTANCE.get();
    }

    private static class LocalHostCacheHeaderBuilder extends CacheHeaderBuilder {
        private static final String X_CACHE_FORMAT = "%s from HTTPCache4j(%s)";

        private String getHostAddress() {
            String hostname;
            try {
                hostname = InetAddress.getLocalHost().getHostAddress();
            }
            catch (UnknownHostException ex) {
                hostname = "localhost";
            }
            return hostname;
        }

        public Header createMISSXCacheHeader() {
            String value = String.format(X_CACHE_FORMAT, "MISS", getHostAddress());
            return new Header(HeaderConstants.X_CACHE, value);
        }

        public Header createHITXCacheHeader() {
            String value = String.format(X_CACHE_FORMAT, "HIT", getHostAddress());
            return new Header(HeaderConstants.X_CACHE, value);

        }
    }
}
