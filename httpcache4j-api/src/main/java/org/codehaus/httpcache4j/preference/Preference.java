/*
 * Copyright (c) 2008, The Codehaus. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.codehaus.httpcache4j.preference;


import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.codehaus.httpcache4j.Directive;
import org.codehaus.httpcache4j.Directives;
import org.codehaus.httpcache4j.Header;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.util.NumberUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Preference<T> {
    private final T preference;
    private final double quality;

    public Preference(T preference) {
        this(preference, 1.0);
    }

    public Preference(T preference, double quality) {
        Preconditions.checkNotNull(preference, "Preference may not be null, use a ALL preference instead.");
        Preconditions.checkArgument(quality <= 1.0 && quality > 0.0, "Quality is a percentage ranging from 0.0, to 1.0");
        this.preference = preference;
        this.quality = quality;
    }

    public T getPreference() {
        return preference;
    }

    public double getQuality() {
        return quality;
    }

    @Override
    public String toString() {
        StringBuilder headerValue = new StringBuilder();
        if (getQuality() != 1.0) {
            headerValue.append(getStringValue()).append(";q=").append(getQuality());
        }
        else {
            headerValue.append(getStringValue());
        }
        return headerValue.toString();
    }

    protected String getStringValue() {
        return getPreference().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Preference<?> that = (Preference<?>) o;

        if (preference != null ? !preference.equals(that.preference) : that.preference != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return preference != null ? preference.hashCode() : 0;
    }


    public static <T> List<Preference<T>> wrap(T... values) {
        return Lists.transform(Arrays.asList(values), new Function<T, Preference<T>>() {
            @Override
            public Preference<T> apply(T input) {
                return new Preference<T>(input);
            }
        });
    }

    public static <T> Function<T, String> toStringF() {
        return new Function<T, String>() {
            @Override
            public String apply(T input) {
                return input.toString();
            }
        };
    }

    public static <T> Header toHeader(String headerName, List<? extends Preference<T>> preferences, Function<T, String> f) {
        StringBuilder builder = new StringBuilder();
        for (Preference<T> preference : preferences) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(f.apply(preference.getPreference()));
            if (preference.getQuality() != 1.0) {
                builder.append(";q=").append(preference.getQuality());
            }
        }
        return new Header(headerName, builder.toString());
    }

    public static <T> List<Preference<T>> parse(Header header, Function<String, T> f) {
        ImmutableList.Builder<Preference<T>> accept = ImmutableList.builder();
        Directives directives = header.getDirectives();
        for (Directive directive : directives) {
            String loc = directive.getName();
            T value = f.apply(loc);
            if (value == null) {
                throw new IllegalArgumentException("Transformation turned value to null");
            }
            double quality = NumberUtils.toDouble(directive.getParameterValue("q"), 1.0);
            accept.add(new Preference<T>(value, quality));
        }
        return accept.build();
    }

    public static Function<Locale, String> LocaleToString = new Function<Locale, String>() {
        @Override
        public String apply(Locale input) {
            String language = input.getLanguage();
            if (input.getCountry() != null && !input.getCountry().trim().isEmpty()) {
                return language + "-" + input.getCountry().toLowerCase(Locale.ENGLISH);
            }
            return language;
        }
    };

    public static Function<String, Locale> LocaleParse = new Function<String, Locale>() {
        @Override
        public Locale apply(String input) {
            String[] parts = input.split("-", 2);
            return new Locale(parts[0], parts.length == 2 ? parts[1].toUpperCase() : "");
        }
    };

    public static Function<Charset, String> CharsetToString = new Function<Charset, String>() {
        @Override
        public String apply(Charset input) {
            return input.toString();
        }
    };

    public static Function<String, Charset> CharsetParse = new Function<String, Charset>() {
        @Override
        public Charset apply(String input) {
            return new Charset(input);
        }
    };

    public static Function<String, MIMEType> MIMEParse = new Function<String, MIMEType>() {
        @Override
        public MIMEType apply(String input) {
            return MIMEType.valueOf(input);
        }
    };

}
