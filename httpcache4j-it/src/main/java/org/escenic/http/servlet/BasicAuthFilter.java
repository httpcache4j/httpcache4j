package org.escenic.http.servlet;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.codehaus.httpcache4j.HeaderConstants;
import org.escenic.http.Representation;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class BasicAuthFilter extends AbstractEsiFilter {
    private static final String BASIC = "Basic ";
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Override
    public void doFilterImpl(HttpServletRequest pRequest,
                             HttpServletResponse pResponse,
                             FilterChain pChain,
                             PathElement pPath,
                             Representation pRepresentation) throws IOException, ServletException {
        Map<String,String> parameters = pPath.getParameters();
        String user = parameters.get("u");
        String pass = parameters.get("p");
        boolean sendAuth = true;
        if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(pass)) {
            String authorization = pRequest.getHeader(HeaderConstants.AUTHORIZATION);
            if (StringUtils.isNotBlank(authorization)) {
                if (authorization.indexOf(BASIC) != -1) {
                    authorization = authorization.substring(BASIC.length()).trim();
                    String encoded = new String(Base64.encodeBase64((user + ":" + pass).getBytes()));
                    if (encoded.equals(authorization)) {
                        sendAuth = false;
                        pChain.doFilter(pRequest, pResponse);
                    }

                }
            }
        }
        if (sendAuth) {
            sendAuthorization(pResponse);
        }
    }

    private void sendAuthorization(HttpServletResponse pResponse) {
        pResponse.addHeader(HeaderConstants.WWW_AUTHENTICATE, BASIC + "realm=\"TEST\"");
        pResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
