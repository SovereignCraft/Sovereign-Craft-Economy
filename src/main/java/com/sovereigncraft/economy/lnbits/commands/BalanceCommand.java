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
 * Displays the user's global LNBits balance and all wallet balances.
 */
public class BalanceCommand implements CommandExecutor {

    public BalanceCommand() {}
    /**
     * Executes the /balance command.
     *
     * @param sender The command sender (should be a Player).
     * @param command The command being executed.
     * @param label The alias used for the command.
     * @param args The arguments passed to the command.
     * @return true if the command was executed successfully, false otherwise.
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = LNBitsUtils.requirePlayer(sender, "balance");
        if (player == null) return true;

        Map<String, Object> user = LNBitsCacheUsers.getOrFetchAndCacheUserWithWallets(player.getUniqueId());
        if (user == null || !user.containsKey("wallets")) {
            sender.sendMessage("§cNo Lightning wallet found for your account.");
            return true;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> wallets = (List<Map<String, Object>>) user.get("wallets");
        if (wallets.isEmpty()) {
            sender.sendMessage("§cNo Lightning wallet found for your account.");
            return true;
        }

        // Global balance
        Object userBalObj = user.get("balance_msat");
        double userSats = userBalObj instanceof Number ? ((Number) userBalObj).doubleValue() / 1000.0 : 0.0;

        sender.sendMessage("§6" + player.getName() + "'s LNBits Balances:");
        sender.sendMessage("§7Global Balance: §f" + LNBitsUtils.formatSats(userSats) + " sats");

        for (Map<String, Object> wallet : wallets) {
            String name = (String) wallet.getOrDefault("name", "Unnamed Wallet");
            Object balObj = wallet.get("balance_msat");
            double sats = balObj instanceof Number ? ((Number) balObj).doubleValue() / 1000.0 : 0.0;
            sender.sendMessage("§7" + name + ": §f" + LNBitsUtils.formatSats(sats) + " sats");
        }

        return true;
    }
}
