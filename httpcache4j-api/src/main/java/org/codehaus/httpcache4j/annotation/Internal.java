package org.codehaus.httpcache4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anything annotated with this one should be considered an internal API.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
@Retention(value = RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Internal {
}
