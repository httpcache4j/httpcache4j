package org.codehaus.httpcache4j;

import static org.codehaus.httpcache4j.HeaderConstants.*;
import org.codehaus.httpcache4j.preference.Preference;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HTTPUtils {
    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String NO_STORE_HEADER_VALUE = "no-store";
    private static final String NO_CACHE_HEADER_VALUE = "no-cache";

    private HTTPUtils() {
    }

    //TODO: replace with JodaTime DateTimeFormatter... Something weird is going on here
    public static DateTime fromHttpDate(Header header) {
        SimpleDateFormat format = new SimpleDateFormat(PATTERN_RFC1123);
        Date date = null;
        try {
            date = format.parse(header.getValue());
        } catch (ParseException e) {
            //TODO: decide what to do here....
        }
        if (date != null) {
            return new DateTime(date.getTime());
        }
        return null;

        //DateTimeFormatter formatter = DateTimeFormat.forPattern(PATTERN_RFC1123).withZone(DateTimeZone.forID("UTC"));
        //return formatter.parseDateTime(header.getValue());
    }

    public static Header toHttpDate(String headerName, DateTime time) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(PATTERN_RFC1123);
        return new Header(headerName, formatter.print(time));
    }

    public static long getHeaderAsDate(Header header) {
        try {
            DateTime dateTime = fromHttpDate(header);
            if (dateTime != null) {
                return dateTime.getMillis();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return -1;
    }

    public static boolean hasCacheableHeaders(Headers headers) {
        if (headers.contains(new Header(VARY, "*"))) {
            return false;
        }
        if (headers.contains(new Header(CACHE_CONTROL, NO_STORE_HEADER_VALUE)) || headers.contains(new Header(CACHE_CONTROL, NO_CACHE_HEADER_VALUE))) {
            return false;
        }
        if (headers.contains(new Header(PRAGMA, NO_CACHE_HEADER_VALUE))) {
            return false;
        }

        return headers.getFirstHeader(ETAG) != null ||
                headers.getFirstHeader(EXPIRES) != null ||
                headers.getFirstHeader(LAST_MODIFIED) != null;
    }

    public static Header toHeader(String headerName, List<? extends Preference<?>> preferences) {
        StringBuilder builder = new StringBuilder();
        for (Preference preference : preferences) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(preference.toString());
        }
        return new Header(headerName, builder.toString());
    }
    
}