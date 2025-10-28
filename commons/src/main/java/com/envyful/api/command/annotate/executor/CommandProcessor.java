package com.envyful.api.command.annotate.executor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * Annotation for specifying which methods are command processors in the command classes.
 * Allows the developer to specify the minimum args required for this method to be executed, the main sub-command, for
 * this method to be executed, and if it should be run asynchronously (which defaults to true).
 *
 * All commands should be executed off the main thread unless they edit something that could break Minecraft. (i.e. the
 * World)
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandProcessor {

    String value() default "";

    int minArgs() default 0;

    boolean executeAsync() default true;

}
