package com.envyful.api.player.save.attribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * Used for specifying the SQL statements for this class
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Queries {

    String[] value();

    String updateQuery();

    String loadQuery();

}
