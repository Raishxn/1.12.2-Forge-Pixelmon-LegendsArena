package com.envyful.api.command.annotate.executor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * Annotation to be used on parameters for specifying particular argument types for injection purposes.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Argument {

    boolean tabComplete() default false;

    String defaultValue() default "";

}
