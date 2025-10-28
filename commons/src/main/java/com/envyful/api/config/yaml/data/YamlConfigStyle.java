package com.envyful.api.config.yaml.data;

import org.spongepowered.configurate.yaml.NodeStyle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

/**
 *
 * A class to dictate the node style of a YAML configuration
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface YamlConfigStyle {

    NodeStyle value() default NodeStyle.BLOCK;

}
