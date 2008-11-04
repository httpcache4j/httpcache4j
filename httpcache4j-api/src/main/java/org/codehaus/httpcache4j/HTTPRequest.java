package org.codehaus.httpcache4j;

import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.preference.Preferences;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class HTTPRequest {
    private final URI requestURI;
    private final Headers headers;
    private final List<Parameter> parameters;
    private final HTTPMethod method;
    private Challenge challenge;
    private Payload payload;
    private Conditionals conditionals;
    private Preferences preferences;

    public HTTPRequest(URI requestURI, HTTPMethod method) {
        this.method = method;
        this.requestURI = requestURI;
        this.headers = new Headers();
        this.parameters = new ArrayList<Parameter>();
        conditionals = new Conditionals();
        preferences = new Preferences();
    }

    public URI getRequestURI() {
        return requestURI;
    }

    public Headers getHeaders() {
        return headers;
    }

    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public void addHeader(Header header) {
        Validate.notNull(header, "You may not add a null header");
        headers.add(header);
    }

    public void addParameter(Parameter parameter) {
        if (!parameters.contains(parameter)) {
            parameters.add(parameter);
        }
    }

    public Conditionals getConditionals() {
        return conditionals;
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}