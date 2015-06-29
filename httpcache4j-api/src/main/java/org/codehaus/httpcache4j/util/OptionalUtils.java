package org.codehaus.httpcache4j.util;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class OptionalUtils {
    public static <A> Stream<A> stream(Optional<A> opt) {
        return opt.isPresent() ? Stream.of(opt.get()) : Stream.empty();
    }

    public static <A> boolean exists(Optional<A> opt, Predicate<A> p) {
        return opt.filter(p).isPresent();
    }

    public static <A> boolean forall(Optional<A> opt, Predicate<A> p) {
        return stream(opt).allMatch(p);
    }
}
