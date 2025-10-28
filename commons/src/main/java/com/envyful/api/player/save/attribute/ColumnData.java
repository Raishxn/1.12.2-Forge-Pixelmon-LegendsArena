package com.envyful.api.player.save.attribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * Currently only used for specifying the SQL column name of a variable in an
 * {@link com.envyful.api.player.attribute.PlayerAttribute}
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColumnData {

    String value();

}
