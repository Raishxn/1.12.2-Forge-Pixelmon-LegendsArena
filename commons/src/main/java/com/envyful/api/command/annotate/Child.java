package com.envyful.api.command.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * Flags a command class as a "child" (sub-command) class so that when registering classes it's not attempted to be
 * registered as a main command.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Child {
}
