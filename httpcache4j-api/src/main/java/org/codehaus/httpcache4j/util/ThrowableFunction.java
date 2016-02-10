package org.codehaus.httpcache4j.util;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowableFunction<A, B, E extends Exception> {
    B apply(A input) throws E;

    @SuppressWarnings("unchecked")
    static <T extends Throwable> T sneakyRethrow(Throwable t) throws T {
        throw (T) t;
    }

    default Function<A, B> toFunction() {
        return a -> {
            try {
                return this.apply(a);
            } catch (Exception e) {
                throw ThrowableFunction.<Error>sneakyRethrow(e);
            }
        };
    }

    static <A, B, E extends Exception> Function<A, B> lift(ThrowableFunction<A, B, E> f) {
        return f.toFunction();
    }
}
