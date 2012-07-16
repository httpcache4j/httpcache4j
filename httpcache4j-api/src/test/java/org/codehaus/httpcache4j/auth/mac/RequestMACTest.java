package org.codehaus.httpcache4j.auth.mac;

import java.net.URI;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import org.codehaus.httpcache4j.HTTPMethod;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.util.Base64;
import org.joda.time.DateTimeUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class RequestMACTest {

    @Test
    public void verifyNormalizedStringInExampleInDraft() throws Exception {
        String example = "264095" + RequestMAC.NEWLINE +
             "7d8f3e4a" + RequestMAC.NEWLINE +
             "POST" + RequestMAC.NEWLINE +
             "/request?b5=%3D%253D&a3=a&c%40=&a2=r%20b&c2&a3=2+q" + RequestMAC.NEWLINE +
             "example.com" + RequestMAC.NEWLINE +
             "80" + RequestMAC.NEWLINE +
             "a,b,c" + RequestMAC.NEWLINE;

        DateTimeUtils.setCurrentMillisFixed(264095 * 1000L);
        HTTPRequest request = new HTTPRequest(URI.create("http://example.com/request?b5=%3D%253D&a3=a&c%40=&a2=r%20b&c2&a3=2+q"), HTTPMethod.POST);
        RequestMAC mac = new RequestMAC(null, new Nonce("7d8f3e4a"), "a,b,c");
        String normalized = mac.toNormalizedRequestString(request);
        assertEquals(example, normalized);
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void verifyMACTokenInExampleInDraft() throws Exception {
        URI uri = URI.create("http://example.com/resource/1?b=1&a=2");
        DateTimeUtils.setCurrentMillisFixed(1336363200 * 1000L);
        Nonce n = new Nonce("dj83hs9s");
        RequestMAC mac = new RequestMAC("489dks293j39", n, null);
        String calculated = mac.getMac(new HTTPRequest(uri), Algorithm.HMAC_SHA_1);
        assertEquals("6T3zZzy2Emppni6bzL7kdRxUWL4=", calculated);
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void verifyMACTokenInExampleWithBodyExtensionMD5() throws Exception {
        URI uri = URI.create("http://example.com/request?b5=%3D%253D&a3=a&c%40=&a2=r%20b&c2&a3=2+q");
        DateTimeUtils.setCurrentMillisFixed(264095 * 1000L);
        Nonce n = new Nonce("7d8f3e4a");
        String ext = String.format("bodyhash=\"%s\"", Base64.encodeBytes(Hashing.md5().hashString("Hello World!", Charsets.UTF_8).asBytes()));
        
        RequestMAC mac = new RequestMAC("2134gfdg", n, ext);
        String calculated = mac.getMac(new HTTPRequest(uri, HTTPMethod.POST), Algorithm.HMAC_SHA_1);
        assertEquals("emuqa52lvjl2WWKD0nSDv/rgniA=", calculated);
        DateTimeUtils.setCurrentMillisSystem();
    }
}
