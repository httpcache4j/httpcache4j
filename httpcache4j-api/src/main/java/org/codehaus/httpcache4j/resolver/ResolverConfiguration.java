package org.codehaus.httpcache4j.resolver;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.codehaus.httpcache4j.auth.*;
import org.codehaus.httpcache4j.util.PropertiesLoader;

import java.util.Properties;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class ResolverConfiguration {
    public static final String DEFAULT_USER_AGENT = "HTTPCache4j " + getVersionFromProperties();

    private static String getVersionFromProperties() {
        final Properties properties = PropertiesLoader.get(ResolverConfiguration.class.getResourceAsStream("/version.properties"));
        String version = properties.getProperty("version");
        if (Strings.isNullOrEmpty(version) || version.contains("${")) {
            return "Development";
        }
        return version;
    }


    private final String userAgent;
    private final boolean useChunked;
    private final ProxyAuthenticator proxyAuthenticator;
    private final Authenticator authenticator;

    public ResolverConfiguration(String userAgent, boolean useChunked, ProxyAuthenticator proxyAuthenticator, Authenticator authenticator) {
        this.userAgent = Preconditions.checkNotNull(userAgent, "User Agent may not be null");
        this.useChunked = useChunked;
        this.proxyAuthenticator = Preconditions.checkNotNull(proxyAuthenticator, "Proxy Authenticator may not be null");
        this.authenticator = Preconditions.checkNotNull(authenticator, "Authenticator may not be null");
    }

    public ResolverConfiguration(ProxyAuthenticator proxyAuthenticator, Authenticator authenticator) {
        this(DEFAULT_USER_AGENT, false, proxyAuthenticator, authenticator);
    }

    public ResolverConfiguration() {
        this(DEFAULT_USER_AGENT, false, new DefaultProxyAuthenticator(), new DefaultAuthenticator());
    }

    public ResolverConfiguration(String userAgent, boolean useChunked) {
        this(userAgent, useChunked, new DefaultProxyAuthenticator(), new DefaultAuthenticator());
    }

    public String getUserAgent() {
        return userAgent;
    }

    public boolean isUseChunked() {
        return useChunked;
    }

    public ProxyAuthenticator getProxyAuthenticator() {
        return proxyAuthenticator;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }
}
