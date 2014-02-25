package org.codehaus.httpcache4j.resolver;

import net.hamnaberg.funclite.Preconditions;
import org.codehaus.httpcache4j.auth.*;
import org.codehaus.httpcache4j.util.PropertiesLoader;
import org.codehaus.httpcache4j.util.StringUtils;

import java.util.Properties;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class ResolverConfiguration {
    public static final String DEFAULT_USER_AGENT = "HTTPCache4j " + getVersionFromProperties();

    private static String getVersionFromProperties() {
        final Properties properties = PropertiesLoader.get(ResolverConfiguration.class.getResourceAsStream("/version.properties"));
        String version = properties.getProperty("version");
        if (StringUtils.isNullOrEmpty(version) || version.contains("${")) {
            return "Development";
        }
        return version;
    }


    private final String userAgent;
    private final boolean useChunked;
    private final ProxyAuthenticator proxyAuthenticator;
    private final Authenticator authenticator;
    private final ConnectionConfiguration connectionConfiguration;

    public ResolverConfiguration(String userAgent, boolean useChunked, ProxyAuthenticator proxyAuthenticator, Authenticator authenticator, ConnectionConfiguration connectionConfiguration) {
        this.connectionConfiguration = connectionConfiguration;
        this.userAgent = Preconditions.checkNotNull(userAgent, "User Agent may not be null");
        this.useChunked = useChunked;
        this.proxyAuthenticator = Preconditions.checkNotNull(proxyAuthenticator, "Proxy Authenticator may not be null");
        this.authenticator = Preconditions.checkNotNull(authenticator, "Authenticator may not be null");
    }

    public ResolverConfiguration(ProxyAuthenticator proxyAuthenticator, Authenticator authenticator, ConnectionConfiguration connectionConfiguration) {
        this(DEFAULT_USER_AGENT, false, proxyAuthenticator, authenticator, connectionConfiguration);
    }

    public ResolverConfiguration() {
        this(DEFAULT_USER_AGENT, false, new DefaultProxyAuthenticator(), new DefaultAuthenticator(), new ConnectionConfiguration());
    }

    public ResolverConfiguration(String userAgent, boolean useChunked) {
        this(userAgent, useChunked, new DefaultProxyAuthenticator(), new DefaultAuthenticator(), new ConnectionConfiguration());
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

    public ConnectionConfiguration getConnectionConfiguration() {
        return connectionConfiguration;
    }

    public ResolverConfiguration withConnectionConfiguration(ConnectionConfiguration connectionConfiguration) {
        return new ResolverConfiguration(userAgent, useChunked, proxyAuthenticator, authenticator, connectionConfiguration);
    }

    public ResolverConfiguration withAuthenticator(Authenticator authenticator) {
        return new ResolverConfiguration(userAgent, useChunked, proxyAuthenticator, authenticator, connectionConfiguration);
    }

    public ResolverConfiguration withProxyAuthenticator(ProxyAuthenticator proxyAuthenticator) {
        return new ResolverConfiguration(userAgent, useChunked, proxyAuthenticator, authenticator, connectionConfiguration);
    }

    public ResolverConfiguration withUserAgent(String userAgent) {
        return new ResolverConfiguration(userAgent, useChunked, proxyAuthenticator, authenticator, connectionConfiguration);
    }

    public ResolverConfiguration withUserAgent(boolean useChunked) {
        return new ResolverConfiguration(userAgent, useChunked, proxyAuthenticator, authenticator, connectionConfiguration);
    }
}
