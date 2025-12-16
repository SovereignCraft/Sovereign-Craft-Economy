package com.sovereigncraft.economy;

import com.sovereigncraft.economy.commands.*;
import com.sovereigncraft.economy.listeners.MapInitialize;
import com.sovereigncraft.economy.util.QRData;
import lombok.SneakyThrows;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicePriority;
import com.sovereigncraft.economy.eco.VaultImpl;
import com.sovereigncraft.economy.listeners.PlayerJoinListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SCEconomy extends JavaPlugin {
    private static SCEconomy instance;
    private static LNBits eco;

    private static VaultImpl vaultImpl;
    public static HashMap<UUID, QRData> playerQRInterface;
    public static HashMap<String, UUID> pendingInvoices;
    public static HashMap<String, UUID> pendingWithdrawals;
    public static HashMap playerAdminKey;
    public static HashMap playerInKey;
    @SneakyThrows
    @Override
    public void onEnable() {

        // Save your images to the plugin folder.
        // The boolean 'false' means it won't overwrite if the file already exists.
        saveResource("qrbg.png", false);
        saveResource("qrbgsc.png", false);
        saveResource("qrwm.png", false);
        saveResource("paysuccess.png",false );
        saveDefaultConfig();
        instance = this;
        vaultImpl = new VaultImpl();
        if (!setupEconomy()) {
            disable("Economy couldn't be registered, Vault plugin is missing!");
            return;
        }
        this.getLogger().info("Vault found, Economy has been registered.");
        this.getCommand("ln").setExecutor(new LNCommand());
        getCommand("ln").setTabCompleter(new LN_autocompletation());
        this.getCommand("balance").setExecutor(new BalanceCommand());
        this.getCommand("pay").setExecutor(new PayCommand());
        this.getCommand("qrcode").setExecutor(new QRCode());
        Bukkit.getPluginManager().registerEvents(new MapInitialize(), this);
        playerQRInterface = new HashMap<>();
        playerAdminKey = new HashMap<>();
        playerInKey = new HashMap<>();
        pendingInvoices = new HashMap<>();
        pendingWithdrawals = new HashMap<>();
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        eco = new LNBits();
        File mapsData = new File(getDataFolder()+File.separator+"data.yml");
        if (!mapsData.exists()) {
            mapsData.createNewFile();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<String, UUID> entry : new HashMap<>(pendingInvoices).entrySet()) {
                    String paymentHash = entry.getKey();
                    UUID uuid = entry.getValue();
                    try {
                        Map<String, Object> check = getEco().checkInvoice(uuid, paymentHash);
                        if (check != null && check.containsKey("paid") && (boolean) check.get("paid")) {
                            if (playerQRInterface.containsKey(uuid)) {
                                playerQRInterface.get(uuid).paid = true;
                            }
                            pendingInvoices.remove(paymentHash);
                        }
                    } catch (Exception e) {
                        getLogger().warning("Error checking invoice " + paymentHash + ": " + e.getMessage());
                    }
                }
            }
        }.runTaskTimerAsynchronously(this, 0, 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<String, UUID> entry : new HashMap<>(pendingWithdrawals).entrySet()) {
                    String withdrawalId = entry.getKey();
                    UUID uuid = entry.getValue();
                    try {
                        Map<String, Object> check = getEco().checkWithdrawal(uuid, withdrawalId);
                        if (check != null && check.containsKey("used")) {
                            Object usedVal = check.get("used");
                            boolean used = false;
                            if (usedVal instanceof Boolean) {
                                used = (Boolean) usedVal;
                            } else if (usedVal instanceof Number) {
                                used = ((Number) usedVal).intValue() >= 1;
                            }

                            if (used) {
                                if (playerQRInterface.containsKey(uuid)) {
                                    playerQRInterface.get(uuid).paid = true;
                                }
                                pendingWithdrawals.remove(withdrawalId);
                            }
                        }
                    } catch (Exception e) {
                        getLogger().warning("Error checking withdrawal " + withdrawalId + ": " + e.getMessage());
                    }
                }
            }
        }.runTaskTimerAsynchronously(this, 0, 20);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabling SovereignCraft Economy");
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
    public static LNBits getEco() {
        return eco;
    }

    public static HashMap<UUID, QRData> getPlayerQRInterface() {
        return playerQRInterface;
    }
}
