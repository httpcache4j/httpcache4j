package org.codehaus.httpcache4j.spring;

import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.codehaus.httpcache4j.resolver.ResponseResolver;
import org.codehaus.httpcache4j.util.NullInputStream;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.*;

/**
 * @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
 */
public class SpringRestTemplateTest {

    @Test
    public void createRestTemplate() throws Exception {
        RestTemplate template = new RestTemplate(new ResponseResolverFactory(new ResponseResolver() {
            @Override
            public HTTPResponse resolve(HTTPRequest request) throws IOException {
                return new HTTPResponse(new InputStreamPayload(new NullInputStream(1), MIMEType.APPLICATION_OCTET_STREAM), Status.OK, new Headers());
            }

            @Override
            public void shutdown() {
            }
        }));
        String object = template.getForObject(URI.create("http://example.com/resource"), String.class);
        assertNotNull("Object was null", object);
    }

    @Test
    public void checkHeadersRestTemplate() throws Exception {
        RestTemplate template = new RestTemplate(new ResponseResolverFactory(new ResponseResolver() {
            @Override
            public HTTPResponse resolve(HTTPRequest request) throws IOException {
                return new HTTPResponse(
                        new InputStreamPayload(new NullInputStream(1), MIMEType.APPLICATION_OCTET_STREAM), Status.OK,
                        new Headers().withContentType(MIMEType.APPLICATION_OCTET_STREAM));
            }

            @Override
            public void shutdown() {
            }
        }));
        ResponseEntity<String> response = template.exchange(URI.create("http://example.com/resource"), HttpMethod.GET, null, String.class);
        assertNotNull("Object was null", response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        HttpHeaders expected = new HttpHeaders();
        expected.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        assertEquals(expected, response.getHeaders());
        assertEquals(0, response.getHeaders().getAllow().size());
        assertNull(response.getHeaders().getETag());
    }
}
