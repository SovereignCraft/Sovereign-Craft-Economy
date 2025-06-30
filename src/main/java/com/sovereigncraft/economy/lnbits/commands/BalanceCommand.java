package com.sovereigncraft.economy.lnbits.commands;

import com.sovereigncraft.economy.lnbits.LNBitsCacheUsers;
import com.sovereigncraft.economy.lnbits.LNBitsClient;
import com.sovereigncraft.economy.lnbits.LNBitsUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Bukkit command executor for the <code>/balance</code> command.
 * Displays the balance of the player's primary LNBits wallet.
 */
public class BalanceCommand implements CommandExecutor {

    private final LNBitsClient client;

    public BalanceCommand(LNBitsClient client) {
        this.client = client;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = LNBitsUtils.requirePlayer(sender, "balance");
        if (player == null) return true;

        Map<String, Object> user = client.cacheUsers().getOrFetchAndCacheUserWithWallets(player.getUniqueId());
        if (user == null || !user.containsKey("wallets")) {
            sender.sendMessage("No Lightning wallet found for your account.");
            return true;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> wallets = (List<Map<String, Object>>) user.get("wallets");
        if (wallets.isEmpty()) {
            sender.sendMessage("No Lightning wallet found for your account.");
            return true;
        }

        Object balObj = wallets.get(0).get("balance");
        double sats = balObj instanceof Number ? ((Number) balObj).doubleValue() / 1000.0 : 0.0;
        sender.sendMessage("Balance: " + LNBitsUtils.formatSats(sats) + " sats");
        return true;
    }
}
