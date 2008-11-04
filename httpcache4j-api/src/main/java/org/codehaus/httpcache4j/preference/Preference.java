package org.codehaus.httpcache4j.preference;

import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.HTTPUtils;
import org.codehaus.httpcache4j.Header;

import java.util.Arrays;

public abstract class Preference<T> {
    private T preference;
    private double quality = 1.0;

    public Preference(T preference) {
        Validate.notNull(preference, "Preference may not be null, use a ALL preference instead.");
        this.preference = preference;
    }

    public T getPreference() {
        return preference;
    }

    public double getQuality() {
        return quality;
    }

    public void setQuality(double quality) {
        Validate.isTrue(quality <= 1.0 && quality > 0.0, "Quality is a percentage ranging from 0.0, to 1.0");
        this.quality = quality;
    }

    @Override
    public String toString() {
        StringBuilder headerValue = new StringBuilder();
        if (getQuality() != 1.0) {
            headerValue.append(getStringValue()).append(";q=").append(getQuality());
        } else {
            headerValue.append(getStringValue());
        }
        return headerValue.toString();
    }

    protected String getStringValue() {
        return getPreference().toString();
    }


    protected Header toHeader() {
        return HTTPUtils.toHeader(getHeaderName(), Arrays.asList(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Preference that = (Preference) o;

        if (preference != null ? !preference.equals(that.preference) : that.preference != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return preference != null ? preference.hashCode() : 0;
    }

    protected abstract String getHeaderName();
}