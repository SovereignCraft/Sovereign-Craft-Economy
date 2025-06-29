package com.sovereigncraft.economy.lnbits;

import com.sovereigncraft.economy.SCE;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Vault integration for Sovereign Craft Economy using LNBits.
 */
public class LNBitsVault implements Economy {

    private final LNBitsClient lnbits = SCE.getClient();

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
        String username = LNBitsUtils.uuidToUsername(player.getUniqueId());
        return LNBitsCache.getCachedUser(username) != null;
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
            return false;
        }
        return hasAccount(p);
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        String username = LNBitsUtils.uuidToUsername(player.getUniqueId());
        Map<String, Object> user = LNBitsCache.getCachedUser(username);
        if (user != null && user.containsKey("wallets")) {
            List<Map<String, Object>> wallets = (List<Map<String, Object>>) user.get("wallets");
            if (!wallets.isEmpty()) {
                Object bal = wallets.get(0).get("balance");
                return bal instanceof Number ? ((Number) bal).doubleValue() / 1000.0 : 0;
            }
        }
        return 0;
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
        if (hasAccount(player)) {
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
