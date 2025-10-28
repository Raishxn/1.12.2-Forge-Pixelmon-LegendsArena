package com.envyful.api.config.type;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public  class RedisDatabaseDetails {

    private String ip;
    private int port;
    private String password;

    public RedisDatabaseDetails() {
    }

    public RedisDatabaseDetails(String ip, int port, String password) {
        this.ip = ip;
        this.port = port;
        this.password = password;
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    public String getPassword() {
        return this.password;
    }
}
