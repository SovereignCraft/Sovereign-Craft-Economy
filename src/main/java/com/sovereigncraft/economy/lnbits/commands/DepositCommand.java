package com.sovereigncraft.economy.lnbits.commands;

import com.sovereigncraft.economy.lnbits.LNBitsClient;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * Bukkit command that allows a player to create a Lightning invoice for
 * depositing satoshis into their LNBits wallet.
 */
public class DepositCommand implements CommandExecutor {
    private final LNBitsClient client;

    /**
     * Creates a new deposit command using the provided LNBits client.
     *
     * @param client LNBits API client
     */
    public DepositCommand(LNBitsClient client) {
        this.client = client;
    }

    /**
     * Handles the {@code /deposit} command, creating a Lightning invoice for
     * the specified satoshi amount.
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

        if (args.length != 1) {
            player.sendMessage("Usage: /deposit <amount-in-sats>");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[0]);
            if (amount <= 0) {
                player.sendMessage("Amount must be greater than 0.");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid amount.");
            return true;
        }

        try {
            UUID uuid = player.getUniqueId();
            Map<String, Object> user = client.users().getUser(uuid);
            String userId = (String) user.get("id");

            Map<String, Object> wallet = client.wallets().getWallet(userId);
            if (wallet == null) {
                player.sendMessage("§cNo wallet found for your account.");
                return true;
            }
            String inkey = (String) wallet.get("inkey");

            // Create invoice
            String bolt11 = client.payments().createInvoice(inkey, amount);
            player.sendMessage("§aInvoice created! Pay this using your wallet:");
            player.sendMessage("§f" + bolt11);

        } catch (Exception e) {
            player.sendMessage("§cError generating invoice: " + e.getMessage());
        }

        return true;
    }
}
