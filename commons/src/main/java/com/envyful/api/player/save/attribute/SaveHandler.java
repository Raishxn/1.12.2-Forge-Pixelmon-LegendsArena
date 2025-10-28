package com.envyful.api.player.save.attribute;

import com.envyful.api.player.save.VariableSaveHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SaveHandler {

    Class<? extends VariableSaveHandler<?>> value();

}
