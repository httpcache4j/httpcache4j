package org.codehaus.httpcache4j.auth.mac;

import java.net.URI;

import com.google.common.base.Strings;

import org.codehaus.httpcache4j.HTTPRequest;

/**
 * http://tools.ietf.org/html/draft-ietf-oauth-v2-http-mac
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class RequestMAC {
    private final String key;
    private final Nonce nonce;
    private final String ext;
    static final String NEWLINE = "\n";
    //static final String NEWLINE = "%x0A";

    public RequestMAC(String key, Nonce nonce, String ext) {
        this.key = key;
        this.nonce = nonce;
        this.ext = Strings.isNullOrEmpty(ext) ? "" : ext;
    }

    public String getKey() {
        return key;
    }

    public Nonce getNonce() {
        return nonce;
    }

    public String getExt() {
        return ext;
    }

    /**
     * 1.  The timestamp value calculated for the request.
     * 2.  The nonce value generated for the request.
     * 3.  The HTTP request method in upper case.  For example: "HEAD",
     * "GET", "POST", etc.
     * 4.  The HTTP request-URI as defined by [RFC2616] section 5.1.2.
     * 5.  The hostname included in the HTTP request using the "Host"
     * request header field in lower case.
     * 6.  The port as included in the HTTP request using the "Host" request
     * header field.  If the header field does not include a port, the
     * default value for the scheme MUST be used (e.g. 80 for HTTP and
     * 443 for HTTPS).
     * 7.  The value of the "ext" "Authorization" request header field
     * attribute if one was included in the request, otherwise, an empty
     * string.
     *
     * @param request The request to build from
     * @return the normalized String as specified above.
     */
    String toNormalizedRequestString(HTTPRequest request) {
        long ts = request.getRequestTime().getMillis() / 1000L; // We need this in seconds.
        URI requestURI = request.getRequestURI();
        StringBuilder pathBuilder = new StringBuilder(requestURI.getRawPath());
        if (requestURI.getQuery() != null) {
            pathBuilder.append("?").append(requestURI.getRawQuery());
        }
        int port = requestURI.getPort();
        if (port == -1) {
            if ("http".equals(requestURI.getScheme())) {
                port = 80;
            } else if ("https".equals(requestURI.getScheme())) {
                port = 443;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ts);
        sb.append(NEWLINE).append(nonce.format());
        sb.append(NEWLINE).append(request.getMethod().getMethod()).append(NEWLINE).append(pathBuilder);
        sb.append(NEWLINE).append(requestURI.getHost()).append(NEWLINE).append(port);
        sb.append(NEWLINE).append(ext).append(NEWLINE);
        return sb.toString();
    }

    public String getMac(HTTPRequest request, Algorithm algorithm) {
        return algorithm.encode(key, toNormalizedRequestString(request));
    }
    
    public String toHeaderValue(HTTPRequest request, String id, Algorithm algorithm) {
        long ts = request.getRequestTime().getMillis() / 1000L; // We need this in seconds.
        return String.format("MAC id=\"%s\", ts=\"%s\", nonce=\"%s\", mac=\"%s\"", id, ts, nonce.format(), getMac(request, algorithm));
    }
}
