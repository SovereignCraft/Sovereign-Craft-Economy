package com.sovereigncraft.economy;

import com.sovereigncraft.economy.lnbits.LNBitsCacheInitializer;
import com.sovereigncraft.economy.lnbits.LNBitsClient;
import com.sovereigncraft.economy.lnbits.commands.BalanceCommand;
import com.sovereigncraft.economy.lnbits.commands.PayCommand;
import com.sovereigncraft.economy.lnbits.data.LNBitsCacheUsersData;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class SCE extends JavaPlugin {
    private static SCE instance;
    private static LNBitsClient client;

    @Override
    public void onEnable() {
        
        saveDefaultConfig();
        instance = this;

        LogHandler.initialize();
        validateConfig();

        LNBitsCacheUsersData.load();

        // Initialize LNBits client
        client = new LNBitsClient();
        LNBitsCacheInitializer.initializeAsync(this);

        // Register wallet command
        registerCommands();

        // Register event listener
        //getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        // Ensure data file exists
        File dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (Exception e) {
                getLogger().warning("Failed to create data.yml file.");
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                player.sendMessage("[SCE] Sovereign Craft Economy v" + getDescription().getVersion() + " enabled");
            }
        }
    }

    @Override
    public void onDisable() {
        LNBitsCacheUsersData.save();
        getLogger().info("Saved LNBits user cache.");
        getLogger().info("Sovereign Craft Economy disabled");
        // Add any future cleanup logic here
    }

    private void validateConfig() {
        if (getConfig().getString("host", "").isEmpty()) {
            getLogger().warning("Config 'host' is not set! LNBits integration may fail.");
        }
        if (getConfig().getString("BearerTokens.default", "").equals("<default-bearer-token>")) {
            getLogger().warning("You are using the placeholder bearer token. Replace it in config.yml.");
        }
    }

    private void registerCommands() {
        if (getCommand("wallet") != null) {
            //getCommand("wallet").setExecutor(new WalletCommand(client));
        }
        if (getCommand("balance") != null) {
            getCommand("balance").setExecutor(new BalanceCommand());
        }
        if (getCommand("pay") != null) {
            getCommand("pay").setExecutor(new PayCommand());
        }
        // Future commands can be added here
    }

    public void reload() {
        reloadConfig();
        getLogger().info("Sovereign Craft Economy configuration reloaded.");
    }

    public static boolean isDebug() {
        return getInstance().getConfig().getBoolean("DEBUG", false);
    }

    public static SCE getInstance() {
        return instance;
    }

    public static LNBitsClient getClient() {
        return client;
    }
}
