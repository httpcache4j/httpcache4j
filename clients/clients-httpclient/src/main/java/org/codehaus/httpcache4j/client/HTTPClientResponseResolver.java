package org.codehaus.httpcache4j.client;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.*;

import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.resolver.PayloadCreator;
import org.codehaus.httpcache4j.resolver.ResponseResolver;
import org.codehaus.httpcache4j.resolver.AbstractResponseResolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * An implementation of the ResponseResolver using the Commons HTTPClient (http://hc.apache.org/httpclient-3.x/)
 * <p/>
 * If you need to use SSL, please follow the guide here.
 * http://hc.apache.org/httpclient-3.x/sslguide.html
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class HTTPClientResponseResolver extends AbstractResponseResolver {
    private final HttpClient client;
    private boolean useRequestChallenge = true;

    public HTTPClientResponseResolver(HttpClient client, PayloadCreator payloadCreator) {
        super(payloadCreator);
        this.client = client;
    }

    public HTTPResponse resolve(HTTPRequest request) {
        HttpMethod method = convertRequest(request);
        try {
            client.executeMethod(method);
            return convertResponse(method);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isUseRequestChallenge() {
        return useRequestChallenge;
    }

    public void setUseRequestChallenge(boolean useRequestChallenge) {
        this.useRequestChallenge = useRequestChallenge;
    }

    private HttpMethod convertRequest(HTTPRequest request) {
        URI requestURI = request.getRequestURI();
        HttpMethod method = getMethod(request.getMethod(), requestURI);
        Headers headers = request.getHeaders();
        addHeaders(headers, method);
        Headers conditionalHeaders = request.getConditionals().toHeaders();
        addHeaders(conditionalHeaders, method);
        Headers preferencesHeaders = request.getPreferences().toHeaders();
        addHeaders(preferencesHeaders, method);
        if (isUseRequestChallenge()) {
            Challenge challenge = request.getChallenge();
            if (challenge != null) {
                method.setDoAuthentication(true);
                Credentials usernamePassword = new UsernamePasswordCredentials(challenge.getIdentifier(), challenge.getPassword() != null ? new String(challenge.getPassword()) : null);
                client.getState().setCredentials(new AuthScope(requestURI.getHost(), requestURI.getPort(), AuthScope.ANY_REALM), usernamePassword);
            }
        }
        List<Parameter> parameters = request.getParameters();
        List<NameValuePair> query = new ArrayList<NameValuePair>(parameters.size());
        for (Parameter parameter : parameters) {
            query.add(new NameValuePair(parameter.getName(), parameter.getValue()));
        }
        method.setQueryString(query.toArray(new NameValuePair[query.size()]));
        return method;
    }

    private void addHeaders(Headers headers, HttpMethod method) {
        for (Map.Entry<String, List<org.codehaus.httpcache4j.Header>> entry : headers) {
            for (org.codehaus.httpcache4j.Header header : entry.getValue()) {
                method.addRequestHeader(header.getName(), header.getValue());
            }
        }
    }

    private HTTPResponse convertResponse(HttpMethod method) {
        Headers headers = new Headers();
        for (Header header : method.getResponseHeaders()) {
            addHeader(headers, header.getName(), header.getValue());
        }
        InputStream stream = getInputStream(method);
        Payload payload;
        if (stream != null) {
            payload = getPayloadCreator().createPayload(headers, stream);
        }
        else {
            payload = null;
        }

        return new HTTPResponse(payload, Status.valueOf(method.getStatusCode()), headers);
    }

    private InputStream getInputStream(HttpMethod method) {
        try {
            return method.getResponseBodyAsStream();
        }
        catch (IOException e) {
            return null;
        }
    }

    private void addHeader(Headers headers, String name, String value) {
        headers.add(new org.codehaus.httpcache4j.Header(name, value));
    }

    HttpMethod getMethod(HTTPMethod method, URI requestURI) {
        switch (method) {
            case GET:
                return new GetMethod(requestURI.toString());
            case HEAD:
                return new HeadMethod(requestURI.toString());
            case OPTIONS:
                return new OptionsMethod(requestURI.toString());
            case TRACE:
                return new TraceMethod(requestURI.toString());
            case PUT:
                return new PutMethod(requestURI.toString());
            case POST:
                return new PostMethod(requestURI.toString());
            case DELETE:
                return new DeleteMethod(requestURI.toString());
            default:
                throw new IllegalArgumentException("Uknown method");
        }
    }
}
