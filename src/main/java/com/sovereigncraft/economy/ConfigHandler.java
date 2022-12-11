package com.sovereigncraft.economy;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class ConfigHandler {
    private static FileConfiguration getConfig() {
        return SCEconomy.getInstance().getConfig();
    }
    public static String getHost() { return getConfig().getString("Host"); }

    public static String getPort() {
        return getConfig().getString("port");
    }

    public static String getAPIKey() {
        return getConfig().getString("APIKey");
    }

    public static String getAdminUser() {
        return getConfig().getString("AdminUser");
    }

    public static UUID getServerUUID() { return UUID.fromString(getConfig().getString("ServerUUID")); }
    public static String getUserID() {
        return getConfig().getString("user-id");
    }

    public static String getAdminKey() {
        return getConfig().getString("admin-key");
    }

    public static String getAdminWallet() {
        return getConfig().getString("adminwallet"); }
    public static Double getStartingBalance() {
        return getConfig().getDouble("startingBalance");
    }
}
