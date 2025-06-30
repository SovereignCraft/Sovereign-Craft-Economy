package com.sovereigncraft.economy;

import com.sovereigncraft.economy.lnbits.LNBitsCacheUsers;
import com.sovereigncraft.economy.lnbits.LNBitsClient;
import com.sovereigncraft.economy.lnbits.LNBitsVault;
import com.sovereigncraft.economy.lnbits.commands.WalletCommand;
import com.sovereigncraft.economy.lnbits.commands.BalanceCommand;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;

import java.io.File;

public final class SCE extends JavaPlugin {
    private static SCE instance;
    private static LNBitsClient client;
    private static LNBitsVault vault;

    @Override
    public void onEnable() {
        
        saveDefaultConfig();
        instance = this;

        LogHandler.initialize();
        validateConfig();

        // Initialize LNBits client
        client = new LNBitsClient();
        LNBitsCacheUsers.initializeAsync(this);

        // Register Vault Economy
        if (!setupVault()) {
            getLogger().warning("Vault plugin not found or economy registration failed.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

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
        getLogger().info("Sovereign Craft Economy disabled");
        // Add any future cleanup logic here
    }

    private boolean setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        vault = new LNBitsVault();
        getServer().getServicesManager().register(Economy.class, vault, this, ServicePriority.Highest);
        return true;
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
            getCommand("wallet").setExecutor(new WalletCommand(client));
        }
        if (getCommand("balance") != null) {
            getCommand("balance").setExecutor(new BalanceCommand(client));
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

    public static LNBitsVault getVault() {
        return vault;
    }
}
