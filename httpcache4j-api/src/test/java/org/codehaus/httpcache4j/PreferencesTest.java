package org.codehaus.httpcache4j;

import org.junit.Assert;
import org.codehaus.httpcache4j.preference.Preference;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
 */
public class PreferencesTest {
    @Test
    public void parseLocales() throws Exception {
        Header header = new Header(HeaderConstants.ACCEPT_LANGUAGE, "da, en-gb;q=0.8, en;q=0.7");
        Headers headers = new Headers().add(header);
        List<Preference> acceptLanguage = headers.getAcceptLanguage();
        Assert.assertEquals(
                Arrays.asList(new Preference("da"), new Preference("en-gb", 0.8), new Preference("en", 0.7)),
                acceptLanguage
        );
    }

    @Test
    public void parseUnsortedLocales() throws Exception {
        Header header = new Header(HeaderConstants.ACCEPT_LANGUAGE, "en-gb;q=0.8, da, en;q=0.7");
        Headers headers = new Headers().add(header);
        List<Preference> acceptLanguage = headers.getAcceptLanguage();
        Assert.assertEquals(
                Arrays.asList(new Preference("da"), new Preference("en-gb", 0.8), new Preference("en", 0.7)),
                acceptLanguage
        );
    }

    @Test
    public void parseCharsets() throws Exception {
        Header header = new Header(HeaderConstants.ACCEPT_CHARSET, "unicode, ISO-8859-1; q=0.9, US-ASCII; q=0.8");
        Headers headers = new Headers().add(header);
        List<Preference> acceptCharset = headers.getAcceptCharset();
        Assert.assertEquals(
                Arrays.asList(new Preference("unicode"), new Preference("ISO-8859-1", 0.9), new Preference("US-ASCII", 0.8)),
                acceptCharset
        );
    }
}
