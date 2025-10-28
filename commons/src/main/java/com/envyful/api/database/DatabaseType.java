package com.envyful.api.database;

import com.envyful.api.database.impl.SimpleHikariDatabase;
import com.envyful.api.database.impl.SimpleJedisDatabase;
import com.google.common.collect.Maps;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Arrays;
import java.util.Map;

/**
 *
 * Enum representing all the potential database types supported by this API
 *
 */
public enum DatabaseType {

    MYSQL("mysql") {
        @Override
        public Database getDatabase(ConfigurationNode node) {
            String id = node.node("id").getString();
            String ip = node.node("ip").getString();
            int port = node.node("port").getInt();
            String username = node.node("username").getString();
            String password = node.node("password").getString();
            String databaseName = node.node("database").getString();

            return new SimpleHikariDatabase(id, ip, port, username, password, databaseName);
        }
    },
    REDIS("redis") {
        @Override
        public Database getDatabase(ConfigurationNode node) {
            String ip = node.node("ip").getString();
            int port = node.node("port").getInt();
            String password = node.node("password").getString();

            return new SimpleJedisDatabase(ip, port, password);
        }
    }

    ;

    private static final Map<String, DatabaseType> DATABASES = Maps.newHashMap();

    static {
        Arrays.stream(values()).forEach(type -> DATABASES.put(type.getConfigId().toLowerCase(), type));
    }

    private final String configId;

    DatabaseType(String configId) {
        this.configId = configId;
    }

    public String getConfigId() {
        return this.configId;
    }

    /**
     *
     * Loads the database from configuration settings
     *
     * @param node The config node
     * @return The loaded database
     */
    public abstract Database getDatabase(ConfigurationNode node);

    /**
     *
     * Gets the database type from the id
     *
     * @param id The id
     * @return The database type
     */
    public static DatabaseType fromId(String id) {
        return DATABASES.get(id.toLowerCase());
    }
}
