package com.sovereigncraft.economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicePriority;
import com.sovereigncraft.economy.commands.BalanceCommand;
import com.sovereigncraft.economy.commands.PayCommand;
import com.sovereigncraft.economy.eco.*;
import com.sovereigncraft.economy.listeners.PlayerJoinListener;

public final class SCEconomy extends JavaPlugin {

    private static lnbits eco;
    private static SCEconomy instance;
    private static VaultImpl vaultImpl;
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        instance = this;
        vaultImpl = new VaultImpl();
        if (!setupEconomy()) {
            disable("Economy couldn't be registed, Vault plugin is missing!");
            return;
        }
        this.getCommand("balance").setExecutor(new BalanceCommand());
        this.getCommand("pay").setExecutor(new PayCommand());
        this.getLogger().info("Vault found, Economy has been registered.");
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        eco = new lnbits();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public static void disable(String message) {
        warn(message);
        Bukkit.getPluginManager().disablePlugin(SCEconomy.getInstance());
    }
    public static void warn(String message) {
        SCEconomy.getInstance().getLogger().warning(message);
    }
    public static SCEconomy getInstance() {
        return instance;
    }
    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        this.getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class, vaultImpl, this,
                ServicePriority.Highest);
        return true;
    }
    public static lnbits getEco() {
        return eco;
    }
}
