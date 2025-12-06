package com.sovereigncraft.economy;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class ConfigHandler {
    private static FileConfiguration getConfig() {
        return SCEconomy.getInstance().getConfig();
    }
    public static String getHost() { 
        return getConfig().getString("host");
    }
    public static String getPubHost() { 
        return getConfig().getString("pubhost");
    }
    public static String getPort() {
        return getConfig().getString("port");
    }
    public static String getAccessToken() { return getConfig().getString("accessToken"); }
    public static UUID getServerUUID() { 
        return UUID.fromString(getConfig().getString("ServerUUID"));
    }
    public static String getAdminKey() {
        return getConfig().getString("AdminKey");
    }
    public static String getGlobalAdminKey() { return getConfig().getString("GlobalAdminKey"); }

    public static Boolean getLNAddress() {
        return (getConfig().getBoolean("lnaddress"));
    }

    public static Double getStartingBalance() {
        return getConfig().getDouble("startingBalance");
    }
    public static String getBedrockPrefix() { return getConfig().getString("bedrockPrefix"); }
    public static String getLNBitsBedrockSuffix() {
        return getConfig().getString("lnbitsbedrocksuffix", ".b");
    }
}
