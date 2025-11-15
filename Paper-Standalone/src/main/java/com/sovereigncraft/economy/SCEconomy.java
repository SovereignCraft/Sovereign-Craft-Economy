package com.sovereigncraft.economy;

import com.sovereigncraft.economy.commands.*;
import com.sovereigncraft.economy.eco.VaultImpl;
import com.sovereigncraft.economy.listeners.MapInitialize;
import com.sovereigncraft.economy.listeners.PlayerJoinListener;
import lombok.Getter;
import lombok.SneakyThrows;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public final class SCEconomy extends JavaPlugin {
    @Getter
    private static SCEconomy instance;
    @Getter
    private LNBits lnbits;
    @Getter
    private VaultImpl vaultImpl;

    public static HashMap playerQRInterface;
    public static HashMap playerAdminKey;
    public static HashMap playerInKey;

    @SneakyThrows
    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;

        ConfigHandler configHandler = new ConfigHandler(getConfig());
        PlayerBridge playerBridge = new BukkitPlayerBridge();

        lnbits = new LNBits(
                configHandler.getHost(),
                Integer.parseInt(configHandler.getPort()),
                configHandler.getAdminKey(),
                configHandler.getAccessToken(),
                configHandler.getBedrockPrefix(),
                configHandler.getLNBitsBedrockSuffix(),
                configHandler.getStartingBalance(),
                configHandler.getServerUUID(),
                playerBridge,
                getLogger()
        );

        vaultImpl = new VaultImpl(this);
        if (!setupEconomy()) {
            disable("Economy couldn't be registered, Vault plugin is missing!");
            return;
        }
        this.getLogger().info("Vault found, Economy has been registered.");

        this.getCommand("ln").setExecutor(new LNCommand(this));
        getCommand("ln").setTabCompleter(new LN_autocompletation(this));
        this.getCommand("balance").setExecutor(new BalanceCommand(this));
        this.getCommand("pay").setExecutor(new PayCommand(this));
        this.getCommand("qrcode").setExecutor(new QRCode());

        Bukkit.getPluginManager().registerEvents(new MapInitialize(), this);
        playerQRInterface = new HashMap<>();
        playerAdminKey = new HashMap<>();
        playerInKey = new HashMap<>();
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        File mapsData = new File(getDataFolder() + File.separator + "data.yml");
        if (!mapsData.exists()) {
            mapsData.createNewFile();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling SovereignCraft Economy");
    }

    public static void disable(String message) {
        warn(message);
        Bukkit.getPluginManager().disablePlugin(getInstance());
    }

    public static void warn(String message) {
        getInstance().getLogger().warning(message);
    }

    public static String getMessage(String messageCode) {
        return getInstance().getConfig().getString(messageCode).replace("&", "§");
    }

    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        this.getServer().getServicesManager().register(Economy.class, vaultImpl, this, ServicePriority.Highest);
        return true;
    }
}
