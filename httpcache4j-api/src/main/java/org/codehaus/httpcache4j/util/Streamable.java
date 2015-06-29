package org.codehaus.httpcache4j.util;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by maedhros on 29/06/15.
 */
public interface Streamable<A> extends Iterable<A> {

    default Stream<A> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

}
