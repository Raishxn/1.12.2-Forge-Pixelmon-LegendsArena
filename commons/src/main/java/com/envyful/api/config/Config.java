package com.envyful.api.config;

import org.spongepowered.configurate.ConfigurationNode;

/**
 *
 * Interface that represents a config class
 *
 */
public interface Config {

    /**
     *
     * Gets the main configuration node of the config
     *
     * @return The main node
     */
    ConfigurationNode getNode();

    /**
     *
     * Saves the {@link Config#getNode()} to the file
     *
     */
    void save();

}
