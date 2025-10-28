package com.envyful.api.command.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * Main command annotation for specifying a command class. Specified the name, aliases, and description of the command.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {

    /**
     *
     * The name (main alias) of the command
     *
     * @return The name of the command
     */
    String value();

    /**
     *
     * All other ways to access the command
     *
     * @return The command's alias
     */
    String[] aliases() default {};

    /**
     *
     * Default error description for the command
     *
     * @return Command's description
     */
    String[] description() default "A server command!";

}
