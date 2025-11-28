package com.sovereigncraft.economy;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class ConfigHandler {
    private final FileConfiguration config;

    public ConfigHandler(FileConfiguration config) {
        this.config = config;
    }

    public UUID getFloatUUID() {
        return UUID.fromString(config.getString("floatUUID"));
    }

    public Double getStartingBalance() {
        return config.getDouble("startingBalance");
    }

    public String getTOSURL() {
        return config.getString("TOSURL");
    }
}