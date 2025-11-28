package com.sovereigncraft.economy.eco;

import com.sovereigncraft.economy.PaperSpoke;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class VaultImpl implements net.milkbowl.vault.economy.Economy {

    private final PaperSpoke plugin;
    private static final long TIMEOUT_SECONDS = 5;

    public VaultImpl(PaperSpoke plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return plugin != null;
    }

    @Override
    public String currencyNamePlural() {
        return "Sats";
    }

    @Override
    public String currencyNameSingular() {
        return "Satoshi";
    }

    @Override
    public String format(double v) {
        BigDecimal bd = new BigDecimal(v).setScale(0, RoundingMode.HALF_EVEN);
        return String.valueOf(bd.doubleValue());
    }

    @Override
    public int fractionalDigits() {
        return -1;
    }

    @Override
    public boolean createPlayerAccount(String name) {
        return createPlayerAccount(Bukkit.getOfflinePlayer(name));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (player.isOnline()) {
            plugin.createPlayerAccount((Player) player);
            return true;
        }
        return false;
    }

    @Override
    public boolean createPlayerAccount(String name, String world) {
        return createPlayerAccount(name);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String world) {
        return createPlayerAccount(player);
    }

    @Override
    public EconomyResponse depositPlayer(String name, double amount) {
        return depositPlayer(Bukkit.getOfflinePlayer(name), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (player.isOnline()) {
            try {
                return plugin.depositPlayer((Player) player, amount).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                plugin.getLogger().log(Level.SEVERE, "Timed out waiting for deposit response from hub for " + player.getName());
                return new EconomyResponse(0, 0, ResponseType.FAILURE, "Request timed out.");
            } catch (InterruptedException | ExecutionException e) {
                plugin.getLogger().log(Level.SEVERE, "Error during deposit for " + player.getName(), e);
                return new EconomyResponse(0, 0, ResponseType.FAILURE, "An error occurred.");
            }
        }
        return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player is not online.");
    }

    @Override
    public EconomyResponse depositPlayer(String name, String world, double amount) {
        return depositPlayer(name, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String world, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public double getBalance(String name) {
        return getBalance(Bukkit.getOfflinePlayer(name));
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        if (player.isOnline()) {
            try {
                return plugin.getBalance((Player) player).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                plugin.getLogger().log(Level.SEVERE, "Timed out waiting for getBalance response from hub for " + player.getName());
                return 0.0;
            } catch (InterruptedException | ExecutionException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting balance for " + player.getName(), e);
                return 0.0;
            }
        }
        // Handle offline players later
        return 0.0;
    }

    @Override
    public double getBalance(String name, String world) {
        return getBalance(name);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public String getName() {
        return "Sovereign-Craft-Economy";
    }

    @Override
    public boolean has(String name, double amount) {
        return has(Bukkit.getOfflinePlayer(name), amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String name, String world, double amount) {
        return has(name, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String world, double amount) {
        return has(player, amount);
    }

    @Override
    public boolean hasAccount(String name) {
        return hasAccount(Bukkit.getOfflinePlayer(name));
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        if (player.isOnline()) {
            plugin.getLogger().log(Level.INFO, "Checking account for player: " + player.getName() + " UUID: " + player.getUniqueId().toString());
            try {
                return plugin.hasAccount((Player) player).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                plugin.getLogger().log(Level.SEVERE, "Timed out waiting for hasAccount response from hub for " + player.getName());
                return false;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error checking account for " + player.getName(), e);
                return false;
            }
        }
        // Handle offline players later
        return false;
    }

    @Override
    public boolean hasAccount(String name, String world) {
        return hasAccount(name);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String world) {
        return hasAccount(player);
    }

    @Override
    public EconomyResponse withdrawPlayer(String name, double amount) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(name), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (player.isOnline()) {
            try {
                return plugin.withdrawPlayer((Player) player, amount).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                plugin.getLogger().log(Level.SEVERE, "Timed out waiting for withdraw response from hub for " + player.getName());
                return new EconomyResponse(0, 0, ResponseType.FAILURE, "Request timed out.");
            } catch (InterruptedException | ExecutionException e) {
                plugin.getLogger().log(Level.SEVERE, "Error during withdrawal for " + player.getName(), e);
                return new EconomyResponse(0, 0, ResponseType.FAILURE, "An error occurred.");
            }
        }
        return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player is not online.");
    }

    @Override
    public EconomyResponse withdrawPlayer(String name, String world, double amount) {
        return withdrawPlayer(name, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String world, double amount) {
        return withdrawPlayer(player, amount);
    }

    // Unimplemented bank support methods
    @Override
    public boolean hasBankSupport() { return false; }
    @Override
    public List<String> getBanks() { return null; }
    @Override
    public EconomyResponse isBankMember(String arg0, String arg1) { return null; }
    @Override
    public EconomyResponse isBankMember(String arg0, OfflinePlayer arg1) { return null; }
    @Override
    public EconomyResponse isBankOwner(String arg0, String arg1) { return null; }
    @Override
    public EconomyResponse isBankOwner(String arg0, OfflinePlayer arg1) { return null; }
    @Override
    public EconomyResponse bankBalance(String arg0) { return null; }
    @Override
    public EconomyResponse bankDeposit(String arg0, double arg1) { return null; }
    @Override
    public EconomyResponse bankHas(String arg0, double arg1) { return null; }
    @Override
    public EconomyResponse bankWithdraw(String arg0, double arg1) { return null; }
    @Override
    public EconomyResponse createBank(String arg0, String arg1) { return null; }
    @Override
    public EconomyResponse createBank(String arg0, OfflinePlayer arg1) { return null; }
    @Override
    public EconomyResponse deleteBank(String arg0) { return null; }
}