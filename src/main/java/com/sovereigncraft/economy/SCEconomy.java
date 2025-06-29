package com.sovereigncraft.economy;

import com.sovereigncraft.economy.commands.BalanceCommand;
import com.sovereigncraft.economy.commands.DonateCommand;
import com.sovereigncraft.economy.commands.LNCommand;
import com.sovereigncraft.economy.commands.LN_autocompletation;
import com.sovereigncraft.economy.commands.PayCommand;
import com.sovereigncraft.economy.commands.QRCode;
import com.sovereigncraft.economy.listeners.MapInitialize;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicePriority;
import com.sovereigncraft.economy.eco.VaultImpl;
import com.sovereigncraft.economy.listeners.PlayerJoinListener;
import com.sovereigncraft.economy.lnbits.LNBitsClient;
import com.sovereigncraft.economy.lnbits.commands.WalletCommand;

import java.io.File;
import java.util.HashMap;
//import java.util.List; // isn't used.
import java.util.UUID;

/**
 * Main plugin entry point for the Sovereign Craft Economy.
 * <p>
 * Handles command registration, listener setup and provides access
 * to the configured LNBits integrations.
 */
public final class SCEconomy extends JavaPlugin {
    @Getter @Setter
    private static DonateCommand donateCommand;
    private static SCEconomy instance;
    private static LNBits eco;
    private static LNBitsClient client;

    private static VaultImpl vaultImpl;

    /*
        playerQRInterface – Stores QR code strings (e.g., for syncing wallets or displaying payment info).
        playerAdminKey – Caches LNBits admin keys per player UUID.
        playerInKey – Caches LNBits invoice keys per player UUID.
     */

    public static HashMap<UUID, String> playerQRInterface;
    public static HashMap<UUID, String> playerAdminKey;
    public static HashMap<UUID, String> playerInKey;

    /**
     * Called when the plugin is enabled. This sets up Vault integration,
     * registers commands and listeners and initializes the LNBits client.
     */
    @SneakyThrows
    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        vaultImpl = new VaultImpl();
        if (!setupEconomy()) {
            disable("Economy couldn't be registered, Vault plugin is missing!");
            return;
        }
        this.getLogger().info("Vault found, Economy has been registered.");
        notifyOps("SCE Loaded: " + this.getDescription().getVersion());
        this.getCommand("ln").setExecutor(new LNCommand());
        getCommand("ln").setTabCompleter(new LN_autocompletation());
        this.getCommand("balance").setExecutor(new BalanceCommand());
        this.getCommand("pay").setExecutor(new PayCommand());
        donateCommand = new DonateCommand();
        this.getCommand("qrcode").setExecutor(new QRCode());
        Bukkit.getPluginManager().registerEvents(new MapInitialize(), this);
        playerQRInterface = new HashMap<>();
        playerAdminKey = new HashMap<>();
        playerInKey = new HashMap<>();
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        eco = new LNBits();

        File mapsData = new File(getDataFolder()+File.separator+"data.yml");
        if (!mapsData.exists()) {
            mapsData.createNewFile();
        }

        client = new LNBitsClient();
        this.getCommand("wallet").setExecutor(new WalletCommand(client));

    }

    /**
     * Plugin shutdown handler. Currently only logs to the console.
     */
    @Override
    public void onDisable() {
        getLogger().info("Disabling SovereignCraft Economy");
    }
    /**
     * Disable the plugin with the provided warning message.
     *
     * @param message reason for disabling
     */
    public static void disable(String message) {
        warn(message);
        Bukkit.getPluginManager().disablePlugin(SCEconomy.getInstance());
    }

    /**
     * Log a warning to the plugin logger.
     *
     * @param message warning text
     */
    public static void warn(String message) {
        SCEconomy.getInstance().getLogger().warning(message);
    }
    /**
     * @return singleton instance of the running plugin
     */
    public static SCEconomy getInstance() {
        return instance;
    }
    /**
     * Helper to fetch and translate color codes from config strings.
     */
    public static String getMessage(String messageCode) {
        return getInstance().getConfig().getString(messageCode).replace("&", "§");
    }
    /**
     * Register the Vault economy service.
     */
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
    /**
     * Accessor for the global {@link LNBitsClient} instance.
     */
    public static LNBitsClient getClient() {
        return client;
    }
    //////
    /// 
    /**
     * Send a message to all online operators on the server.
     */
    private void notifyOps(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                player.sendMessage("[OP Only] " + message);
            }
        }
    }

}
