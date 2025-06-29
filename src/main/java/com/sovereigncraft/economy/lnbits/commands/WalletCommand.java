package com.sovereigncraft.economy.lnbits.commands;

import com.sovereigncraft.economy.lnbits.LNBitsClient;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Bukkit command that displays the wallets associated with the executing
 * player, creating a user if none exists.
 */
public class WalletCommand implements CommandExecutor {
    private final LNBitsClient client;

    /**
     * Constructs the wallet command using the given LNBits client.
     *
     * @param client LNBits API client
     */
    public WalletCommand(LNBitsClient client) {
        this.client = client;
    }

    /**
     * Executes the {@code /wallet} command, showing all wallets for the
     * invoking player and creating a user account if necessary.
     *
     * @param sender  the command issuer
     * @param command the executed command
     * @param label   the command alias used
     * @param args    command arguments
     * @return {@code true} always, indicating the command was handled
     */
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
            if (!client.users().userExists(uuid)) {
                player.sendMessage("§eNo wallet found. Creating one...");
                if (!client.users().createUser(uuid)) {
                    player.sendMessage("§cFailed to create wallet.");
                    return true;
                }
            }
            user = client.users().getUser(uuid);

            String userId = (String) user.get("id");
            List<Map<String, Object>> wallets = client.wallets().getWallets(userId);

            player.sendMessage("§aYour Wallets:");
            for (Map<String, Object> wallet : wallets) {
                String name = (String) wallet.get("name");
                double balanceSat = ((Number) wallet.get("balance_msat")).doubleValue() / 1000;
                player.sendMessage("§b" + name + ": §f⚡" + String.format("%,.0f", balanceSat) + " sats");
            }

        } catch (Exception e) {
            player.sendMessage("§cError: " + e.getMessage());
        }
        return true;
    }
}
