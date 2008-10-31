package org.httpcache4j.resolver;

import org.httpcache4j.HTTPRequest;
import org.httpcache4j.HTTPResponse;


public interface ResponseResolver {
    HTTPResponse resolve(HTTPRequest request);
}