package com.envyful.api.command.annotate.executor;

import com.envyful.api.command.injector.TabCompleter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Completable {

    Class<? extends TabCompleter<?, ?>> value();

}
