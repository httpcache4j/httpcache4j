package org.codehaus.httpcache4j.resolver;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;


public interface ResponseResolver {
    HTTPResponse resolve(HTTPRequest request);
}