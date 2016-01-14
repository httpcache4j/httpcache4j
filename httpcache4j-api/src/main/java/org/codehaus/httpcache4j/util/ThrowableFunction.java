package org.codehaus.httpcache4j.util;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowableFunction<A, B, E extends Exception> {
    B apply(A input) throws E;

    default Function<A, B> toFunction() {
        return a -> {
            try {
                return this.apply(a);
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException)e;
                }
                throw new RuntimeException(e);
            }
        };
    }

    static <A, B> ThrowableFunction<A, B, RuntimeException> lift(Function<A, B> f) {
        return f::apply;
    }
}
