package org.httpcache4j.preference;

import org.httpcache4j.Header;
import org.httpcache4j.HeaderConstants;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LocalePreferenceTest {

    @Test
    public void testSingleLocale() {
        Locale us = Locale.US;
        LocalePreference pref = new LocalePreference(us);
        Assert.assertEquals(new Header(HeaderConstants.ACCEPT_LANGUAGE, us.getLanguage()), pref.toHeader());
    }

    @Test
    public void testSingleLocaleWithIgnoredQuality() {
        Locale us = Locale.US;
        LocalePreference pref = new LocalePreference(us);
        pref.setQuality(1.0);
        Assert.assertEquals(new Header(HeaderConstants.ACCEPT_LANGUAGE, us.getLanguage()), pref.toHeader());
    }

    @Test
    public void testSingleLocaleWithQuality() {
        Locale us = Locale.US;
        LocalePreference pref = new LocalePreference(us);
        pref.setQuality(0.8);
        Header expected = new Header(HeaderConstants.ACCEPT_LANGUAGE, us.getLanguage() + ";q=0.8");
        Assert.assertEquals(expected, pref.toHeader());
    }

    @Test
    public void testMultipleLocales() {
        List<Preference> preferences = new ArrayList<Preference>(2);
        for (Locale locale : Arrays.asList(Locale.US, Locale.GERMAN)) {
            preferences.add(new LocalePreference(locale));
        }
        Header expected = new Header(HeaderConstants.ACCEPT_LANGUAGE, Locale.US.getLanguage() + ", " + Locale.GERMAN);
        Assert.assertEquals(expected, LocalePreference.toHeader(HeaderConstants.ACCEPT_LANGUAGE, preferences));
    }
}
