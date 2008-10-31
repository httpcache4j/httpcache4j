package org.httpcache4j.client;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.*;
import org.httpcache4j.*;
import org.httpcache4j.resolver.PayloadCreator;
import org.httpcache4j.resolver.ResponseResolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class HTTPClientResponseResolver implements ResponseResolver {
    private final HttpClient client;
    private final PayloadCreator payloadCreator;

    public HTTPClientResponseResolver(HttpClient client, PayloadCreator payloadCreator) {
        this.client = client;
        this.payloadCreator = payloadCreator;
    }

    public HTTPResponse resolve(HTTPRequest request) {
        HttpMethod method = convertRequest(request);
        try {
            client.executeMethod(method);
            return convertResponse(method);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HttpMethod convertRequest(HTTPRequest request) {
        URI requestURI = request.getRequestURI();
        HttpMethod method = getMethod(request.getMethod(), requestURI);
        Headers headers = request.getHeaders();
        for (Map.Entry<String, List<org.httpcache4j.Header>> headerList : headers) {
            for (org.httpcache4j.Header header : headerList.getValue()) {
                method.addRequestHeader(headerList.getKey(), header.getValue());
            }
        }
        Challenge challenge = request.getChallenge();
        if (challenge != null) {
            method.setDoAuthentication(true);
            Credentials usernamePassword = new UsernamePasswordCredentials(challenge.getIdentifier(), challenge.getPassword() != null ? new String(challenge.getPassword()) : null);
            client.getState().setCredentials(new AuthScope(requestURI.getHost(), requestURI.getPort(), AuthScope.ANY_REALM), usernamePassword);
        }
        return method;
    }

    private HTTPResponse convertResponse(HttpMethod method) {
        Headers headers = new Headers();
        for (Header header : method.getResponseHeaders()) {
            // TODO: shall we split the value on ',' before adding it?
            addHeader(headers, header.getName(), header.getValue());
        }
        return new HTTPResponse(payloadCreator.createPayload(headers, getInputStream(method)), Status.valueOf(method.getStatusCode()), headers);
    }

    private InputStream getInputStream(HttpMethod method) {
        try {
            return method.getResponseBodyAsStream();
        } catch (IOException e) {
            return null;
        }
    }

    private void addHeader(Headers headers, String name, String value) {
        headers.add(new org.httpcache4j.Header(name, value));
    }

    private HttpMethod getMethod(HTTPMethod method, URI requestURI) {
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
