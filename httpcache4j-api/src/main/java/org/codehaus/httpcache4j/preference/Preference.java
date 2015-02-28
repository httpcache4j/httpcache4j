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


import net.hamnaberg.funclite.Preconditions;
import net.hamnaberg.funclite.Optional;
import org.codehaus.httpcache4j.Directive;
import org.codehaus.httpcache4j.Directives;
import org.codehaus.httpcache4j.Header;
import org.codehaus.httpcache4j.util.NumberUtils;

import java.util.*;
import java.util.stream.Collectors;

public final class Preference {
    private final String preference;
    private final double quality;

    public Preference(String preference) {
        this(preference, 1.0);
    }

    public Preference(String preference, double quality) {
        Preconditions.checkArgument(quality <= 1.0 && quality > 0.0, "Quality is a percentage ranging from 0.0, to 1.0");
        this.preference = Objects.requireNonNull(preference, "Preference may not be null, use a ALL preference instead.");
        this.quality = quality;
    }

    public String getPreference() {
        return preference;
    }

    public double getQuality() {
        return quality;
    }

    @Override
    public String toString() {
        StringBuilder headerValue = new StringBuilder();
        if (getQuality() != 1.0) {
            headerValue.append(preference).append(";q=").append(getQuality());
        }
        else {
            headerValue.append(preference);
        }
        return headerValue.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Preference that = (Preference) o;

        if (preference != null ? !preference.equals(that.preference) : that.preference != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return preference != null ? preference.hashCode() : 0;
    }


    public static List<Preference> wrap(String... values) {
        return Collections.unmodifiableList(Arrays.asList(values).stream().map(Preference::new).collect(Collectors.toList()));
    }

    public static Header toHeader(String headerName, List<Preference> preferences) {
        List<Preference> pref = new ArrayList<>(preferences);
        Collections.sort(pref, new PreferenceComparator());

        StringBuilder builder = new StringBuilder();
        for (Preference preference : pref) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(preference.getPreference());
            if (preference.getQuality() != 1.0) {
                builder.append(";q=").append(preference.getQuality());
            }
        }
        return new Header(headerName, builder.toString());
    }

    public static List<Preference> parse(Header header) {
        return parse(Optional.fromNullable(header));
    }

    public static List<Preference> parse(Optional<Header> header) {
        ArrayList<Preference> accept = new ArrayList<>();
        Optional<Directives> dir = header.map(Header::getDirectives);
        Directives directives = dir.getOrElse(new Directives());

        for (Directive directive : directives) {
            String value = directive.getName();
            double quality = NumberUtils.toDouble(directive.getParameterValue("q"), 1.0);
            accept.add(new Preference(value, quality));
        }
        Collections.sort(accept, new PreferenceComparator());
        return Collections.unmodifiableList(accept);
    }

    public static class PreferenceComparator implements Comparator<Preference> {
        @Override
        public int compare(Preference o1, Preference o2) {
            return Double.compare(o2.getQuality(), o1.getQuality());
        }
    }
}
