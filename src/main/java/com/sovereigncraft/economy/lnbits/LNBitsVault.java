package com.sovereigncraft.economy.lnbits;

import com.sovereigncraft.economy.SCE;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;

/**
 * Vault integration for Sovereign Craft Economy using LNBits.
 */
public class LNBitsVault implements Economy {

    private final LNBitsClient lnbits = new LNBitsClient();

    // === Required Vault Methods ===

    @Override
    public boolean isEnabled() {
        return SCE.getInstance() != null;
    }

    @Override
    public String getName() {
        return "Sovereign-Craft-LNBits";
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
    public int fractionalDigits() {
        return -1; // No decimals for Sats
    }

    @Override
    public String format(double amount) {
        return LNBitsUtils.formatSats(amount);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return lnbits.users().userExists(player.getUniqueId());
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Deprecated
    @Override
    public boolean hasAccount(String playerName) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
        if (p == null || !p.hasPlayedBefore()) {
            return false; // Player does not exist
        }
        return p != null && hasAccount(p);
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        try {
            var wallet = lnbits.wallets().getWalletByUUID(uuid, lnbits.users());
            Object bal = wallet.get("balance");
            return bal instanceof Number ? ((Number) bal).doubleValue() / 1000.0 : 0;
        } catch (Exception e) {
            if (SCE.isDebug()) e.printStackTrace();
            return 0;
        }
    }

    @Deprecated
    @Override
    public double getBalance(String playerName) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
        return p != null ? getBalance(p) : 0;
    }

    @Override
    public double getBalance(OfflinePlayer player, String worldName) {
        return getBalance(player);
    }

    @Override
    public double getBalance(String playerName, String worldName) {
        return getBalance(playerName);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Deprecated
    @Override
    public boolean has(String playerName, double amount) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
        return p != null && has(p, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        UUID uuid = player.getUniqueId();
        if (lnbits.users().userExists(uuid)) {
            String inkey = (String) lnbits.wallets().getWalletByUUID(uuid, lnbits.users()).get("inkey");
            String invoice = lnbits.payments().createInvoice(inkey, amount);
            return new EconomyResponse(amount, getBalance(player), ResponseType.SUCCESS, "Issued invoice: " + invoice);
        }
        return new EconomyResponse(0, 0, ResponseType.FAILURE, "User not found");
    }

    @Deprecated
    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
        return p != null ? depositPlayer(p, amount) : new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not found");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (!has(player, amount)) return new EconomyResponse(0, getBalance(player), ResponseType.FAILURE, "Insufficient funds");
        return new EconomyResponse(amount, getBalance(player) - amount, ResponseType.SUCCESS, "Virtual withdrawal acknowledged");
    }

    @Deprecated
    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
        return p != null ? withdrawPlayer(p, amount) : new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not found");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return lnbits.users().createUser(player.getUniqueId());
    }

    @Deprecated
    @Override
    public boolean createPlayerAccount(String playerName) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
        return p != null && createPlayerAccount(p);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    // === Bank Features Not Supported ===

    @Override public boolean hasBankSupport() { return false; }
    @Override public EconomyResponse createBank(String name, String player) { return notSupported(); }
    @Override public EconomyResponse createBank(String name, OfflinePlayer player) { return notSupported(); }
    @Override public EconomyResponse deleteBank(String name) { return notSupported(); }
    @Override public EconomyResponse bankBalance(String name) { return notSupported(); }
    @Override public EconomyResponse bankHas(String name, double amount) { return notSupported(); }
    @Override public EconomyResponse bankWithdraw(String name, double amount) { return notSupported(); }
    @Override public EconomyResponse bankDeposit(String name, double amount) { return notSupported(); }
    @Override public EconomyResponse isBankOwner(String name, String player) { return notSupported(); }
    @Override public EconomyResponse isBankOwner(String name, OfflinePlayer player) { return notSupported(); }
    @Override public EconomyResponse isBankMember(String name, String player) { return notSupported(); }
    @Override public EconomyResponse isBankMember(String name, OfflinePlayer player) { return notSupported(); }
    @Override public List<String> getBanks() { return null; }

    private EconomyResponse notSupported() {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "LNBitsVault does not support banks.");
    }
}