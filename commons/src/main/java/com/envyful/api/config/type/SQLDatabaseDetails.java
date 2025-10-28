package com.envyful.api.config.type;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public  class SQLDatabaseDetails {

    private String poolName;
    private String ip;
    private int port;
    private String username;
    private String password;
    private String database;
    private int maxPoolSize = 30;
    private String connectionUrl = null;
    private long maxLifeTimeSeconds = 30;

    public SQLDatabaseDetails() {
    }

    public SQLDatabaseDetails(String poolName, String ip, int port, String username, String password, String database) {
        this(poolName, ip, port, username, password, database, 30, 30);
    }

    public SQLDatabaseDetails(String poolName, String ip, int port, String username, String password, String database, int maxPoolSize, long maxLifeTimeSeconds) {
        this.poolName = poolName;
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
        this.maxPoolSize = maxPoolSize;
        this.maxLifeTimeSeconds = maxLifeTimeSeconds;
    }

    public String getPoolName() {
        return this.poolName;
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getDatabase() {
        return this.database;
    }

    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    public String getConnectionUrl() {
        return this.connectionUrl;
    }

    public long getMaxLifeTimeSeconds() {
        return this.maxLifeTimeSeconds;
    }
}
