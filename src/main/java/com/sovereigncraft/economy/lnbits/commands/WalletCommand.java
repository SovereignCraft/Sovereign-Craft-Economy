package com.sovereigncraft.economy.lnbits.commands;

import com.sovereigncraft.economy.lnbits.LNBitsClient;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WalletCommand implements CommandExecutor {
    private final LNBitsClient client;

    public WalletCommand(LNBitsClient client) {
        this.client = client;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        try {
            UUID uuid = player.getUniqueId();
            Map<String, Object> user;
            try {
                user = client.users().getUser(uuid);
            } catch (NullPointerException e) {
                player.sendMessage("§eNo wallet found. Creating one...");
                if (!client.users().createUser(uuid)) {
                    player.sendMessage("§cFailed to create wallet.");
                    return true;
                }
                user = client.users().getUser(uuid);
            }

            String userId = (String) user.get("id");
            List<Map<String, Object>> wallets = client.wallets().getWallets(userId);

            player.sendMessage("§aYour Wallets:");
            for (Map<String, Object> wallet : wallets) {
                String name = (String) wallet.get("name");
                double balanceSat = ((Number) wallet.get("balance_msat")).doubleValue() / 1000;
                player.sendMessage("§b" + name + ": §f⚡" + balanceSat + " sats");
            }

        } catch (Exception e) {
            player.sendMessage("§cError: " + e.getMessage());
        }
        return true;
    }
}
