package com.sovereigncraft.economy;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class ConfigHandler {
    private static FileConfiguration getConfig() {
        return SCE.getInstance().getConfig();
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

    public static String getAPIKey() {
        return getConfig().getString("APIKey");
    }

    public static String getAdminUser() {
        return getConfig().getString("AdminUser");
    }

    public static UUID getServerUUID() { 
        return UUID.fromString(getConfig().getString("ServerUUID"));
    }

    public static String getAdminKey() {
        return getConfig().getString("AdminKey");
    }

    public static Boolean getLNAddress() {
        return (getConfig().getBoolean("lnaddress"));
    }

    public static Double getStartingBalance() {
        return getConfig().getDouble("startingBalance");
    }

    public static Boolean getDebug() {
        return getConfig().getBoolean("DEBUG");
    }

    public static String getBearerToken(String key) {
        String specificToken = SCE.getInstance().getConfig().getString("BearerTokens." + key, null);
        
        if (specificToken != null && !specificToken.equalsIgnoreCase("null")) {
            return specificToken;  // Use specific token
        }

        String defaultToken = SCE.getInstance().getConfig().getString("BearerTokens.default", null);
        if (defaultToken != null && !defaultToken.equalsIgnoreCase("null")) {
            return defaultToken;  // Use default token
        }

        return null;  // No token to use
    }

    
}
