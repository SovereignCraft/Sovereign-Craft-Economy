package com.sovereigncraft.economy;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class ConfigHandler {
    private final FileConfiguration config;

    public ConfigHandler(FileConfiguration config) {
        this.config = config;
    }

    public String getHost() {
        return config.getString("host");
    }

    public String getPort() {
        return config.getString("port");
    }

    public String getAccessToken() {
        return config.getString("accessToken");
    }

    public UUID getServerUUID() {
        return UUID.fromString(config.getString("ServerUUID"));
    }

    public String getAdminKey() {
        return config.getString("AdminKey");
    }

    public Double getStartingBalance() {
        return config.getDouble("startingBalance");
    }

    public String getBedrockPrefix() {
        return config.getString("bedrockPrefix");
    }

    public String getLNBitsBedrockSuffix() {
        return config.getString("lnbitsbedrocksuffix", ".b");
    }
}
