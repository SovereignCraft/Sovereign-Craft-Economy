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

    public String getPubHost() {
        return config.getString("pubhost");
    }

    public String getPort() {
        return config.getString("port");
    }

    public String getCookie() {
        return config.getString("cookie");
    }

    public String getAccessToken() {
        return config.getString("accessToken");
    }

    public String getAPIKey() {
        return config.getString("APIKey");
    }

    public String getAdminUser() {
        return config.getString("AdminUser");
    }

    public UUID getServerUUID() {
        return UUID.fromString(config.getString("ServerUUID"));
    }

    public String getAdminKey() {
        return config.getString("AdminKey");
    }

    public Boolean getLNAddress() {
        return (config.getBoolean("lnaddress"));
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
