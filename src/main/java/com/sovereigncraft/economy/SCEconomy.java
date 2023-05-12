package com.sovereigncraft.economy;

import com.sovereigncraft.economy.commands.*;
import com.sovereigncraft.economy.commands.DonateCommand;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicePriority;
import com.sovereigncraft.economy.LNBits;

import com.sovereigncraft.economy.eco.*;
import com.sovereigncraft.economy.eco.VaultImpl;
import com.sovereigncraft.economy.listeners.PlayerJoinListener;

import java.util.ArrayList;
import java.util.Arrays;

public final class SCEconomy extends JavaPlugin {

    private static DonateCommand donateCommand;
    private static SCEconomy instance;
    private static LNBits eco;

    private static VaultImpl vaultImpl;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        vaultImpl = new VaultImpl();
        if (!setupEconomy()) {
            disable("Economy couldn't be registed, Vault plugin is missing!");
            return;
        }
        this.getLogger().info("Vault found, Economy has been registered.");
        this.getCommand("balance").setExecutor(new BalanceCommand());
        //this.getCommand("tos").setExecutor(new TOSCommand());
        this.getCommand("acceptTOS").setExecutor(new AcceptTOSCommand());
        this.getCommand("pay").setExecutor(new PayCommand());
        this.getCommand("deposit").setExecutor(new DepositCommand());
        this.getCommand("webwallet").setExecutor(new WebWalletCommand());
        this.getCommand("refreshwallet").setExecutor(new RefreshWalletCommand());
        donateCommand = new DonateCommand();
        this.getCommand("donate").setExecutor(donateCommand);
        //this.getCommand("donate").setTabCompleter(donateCommand);
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        eco = new LNBits();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("Disabling SovereignCraft Economy");
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
        this.getServer().getServicesManager().register(Economy.class, vaultImpl, this, ServicePriority.Highest);
        return true;
    }
    public static LNBits getEco() {
        return eco;
    }

}
