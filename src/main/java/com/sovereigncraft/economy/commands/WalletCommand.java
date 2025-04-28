package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.LNBits100;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class WalletCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        Player player = (Player) sender;        

        try {
            // Step 1: Get User
            Map<String, Object> user;

            try {
                user = LNBits100.getUser(player.getUniqueId());
            } catch (NullPointerException e) {
                // User not found, create one
                player.sendMessage("§eNo wallet found, creating one...");
                boolean created = LNBits100.createUser(player.getUniqueId());
                if (!created) {
                    player.sendMessage("§cFailed to create wallet.");
                    return true;
                }
                user = LNBits100.getUser(player.getUniqueId()); // Fetch again after creation
            }
            
            String userId = (String) user.get("id");

            // Step 2: Get all wallets
            List<Map<String, Object>> wallets = LNBits100.getWallets(userId);

            // Step 3: Display wallet balances
            player.sendMessage("§aYour Wallets:");
            for (Map<String, Object> wallet : wallets) {
                String walletName = (String) wallet.get("name");
                double balanceMsat = ((Number) wallet.get("balance_msat")).doubleValue();
                double balanceSat = balanceMsat / 1000;
                player.sendMessage("§b" + walletName + ": §f⚡" + balanceSat + " sats");
            }

        } catch (Exception e) {
            player.sendMessage("§cError retrieving wallets: " + e.getMessage());
        }
        return true;
    }
}
