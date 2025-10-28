package com.envyful.api.config.yaml;

import com.envyful.api.config.Config;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;

/**
 *
 * Abstract parent class to all YAML configuration files.
 * Allows all child files to be serializable and when initialised using the {@link YamlConfigFactory} will auto-fill
 * the values from the object to the config file.
 *
 */
@ConfigSerializable
public abstract class AbstractYamlConfig implements Config {

    protected transient ConfigurationReference<CommentedConfigurationNode> base;
    protected transient ValueReference<?, CommentedConfigurationNode> config;

    protected AbstractYamlConfig() {}
    
    @Override
    public ConfigurationNode getNode() {
        return this.config.node();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void save() {
        ((ValueReference <AbstractYamlConfig, ?>) this.config).setAndSave(this);
    }
}
