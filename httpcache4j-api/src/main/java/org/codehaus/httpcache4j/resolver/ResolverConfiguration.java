package org.codehaus.httpcache4j.resolver;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
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
        if (StringUtils.isBlank(version) || version.contains("${")) {
            return "Development";
        }
        return version;
    }


    private final String userAgent;
    private final boolean useChunked;
    private final ProxyAuthenticator proxyAuthenticator;
    private final Authenticator authenticator;

    public ResolverConfiguration(String userAgent, boolean useChunked, ProxyAuthenticator proxyAuthenticator, Authenticator authenticator) {
        Validate.notNull(proxyAuthenticator, "Proxy Authenticator may not be null");
        Validate.notNull(authenticator, "Authenticator may not be null");
        Validate.notNull(userAgent, "User Agent may not be null");
        this.userAgent = userAgent;
        this.useChunked = useChunked;
        this.proxyAuthenticator = proxyAuthenticator;
        this.authenticator = authenticator;
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
