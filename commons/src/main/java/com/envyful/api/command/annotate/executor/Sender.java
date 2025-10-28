package com.envyful.api.command.annotate.executor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * The annotation for specifying that the parameter after this annotation represents the Sender's class type (for
 * injectors to then parse if non-typical platform type)
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Sender {

}
