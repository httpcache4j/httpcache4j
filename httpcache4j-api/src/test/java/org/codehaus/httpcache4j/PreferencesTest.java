package org.codehaus.httpcache4j;

import com.google.common.collect.ImmutableList;
import org.codehaus.httpcache4j.preference.Charset;
import org.junit.Assert;
import org.codehaus.httpcache4j.preference.Preference;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

/**
 * @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
 */
public class PreferencesTest {
    @Test
    public void parseLocales() throws Exception {
        Header header = new Header(HeaderConstants.ACCEPT_LANGUAGE, "da, en-gb;q=0.8, en;q=0.7");
        Headers headers = new Headers().add(header);
        List<Preference<Locale>> acceptLanguage = headers.getAcceptLanguage();
        Assert.assertEquals(
                ImmutableList.of(
                        new Preference<Locale>(new Locale("da")),
                        new Preference<Locale>(new Locale("en", "GB"), 0.8),
                        new Preference<Locale>(Locale.ENGLISH, 0.7)
                )
                , acceptLanguage
        );
    }

    @Test
    public void parseCharsets() throws Exception {
        Header header = new Header(HeaderConstants.ACCEPT_CHARSET, "unicode, ISO-8859-1; q=0.9, US-ASCII; q=0.8");
        Headers headers = new Headers().add(header);
        List<Preference<Charset>> acceptCharset = headers.getAcceptCharset();
        Assert.assertEquals(ImmutableList.of(
                new Preference<Charset>(new Charset("unicode")),
                new Preference<Charset>(new Charset("ISO-8859-1"), 0.9),
                new Preference<Charset>(new Charset("US-ASCII"), 0.8)
        ), acceptCharset);
    }
}
