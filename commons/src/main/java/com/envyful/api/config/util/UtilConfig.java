package com.envyful.api.config.util;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Collections;
import java.util.List;

/**
 *
 * Static utility class for handling configuration methods (such as getting lists)
 *
 */
public class UtilConfig {

    public static <T> List<T> getList(ConfigurationNode node, Class<T> type, Object... path) {
        try {
            return node.node(path).getList(type);
        } catch (SerializationException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

}
