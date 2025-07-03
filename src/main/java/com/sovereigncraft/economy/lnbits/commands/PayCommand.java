package com.sovereigncraft.economy.lnbits.commands;

import com.sovereigncraft.economy.SCE;
import com.sovereigncraft.economy.lnbits.LNBitsClient;
import com.sovereigncraft.economy.lnbits.data.LNBitsUserCache;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Handles the `/pay <minecraftusername> <amount>` command.
 * This command allows one player to send satoshis to another using LNBits.
 * It works by:
 *  - Looking up the recipient's LNBits wallet (creating invoice).
 *  - Using the sender's admin key to pay the invoice.
 *
 * Requirements:
 *  - Both sender and recipient must have LNBits wallets cached.
 *  - LNBitsCacheUsers must support `adminkey()` and `inkey()` retrieval.
 *  - LNBitsClient must be initialized and accessible.
 */
public class PayCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the command is issued by a player (not console)
        if (!(sender instanceof Player player)) return false;

        // Validate argument count: must be /pay <username> <amount>
        if (args.length < 2) {
            player.sendMessage("§cUsage: /pay <username> <amount>");
            return true;
        }

        // Extract recipient username from the first argument
        String recipientName = args[0];

        // Parse the amount as a double (satoshis)
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid amount.");
            return true;
        }

        // Attempt to get the recipient player (must be online)
        Player recipient = Bukkit.getPlayerExact(recipientName);
        if (recipient == null || !recipient.isOnline()) {
            player.sendMessage("§cPlayer not found or offline.");
            return true;
        }

        // Get UUIDs for sender and recipient
        UUID senderUUID = player.getUniqueId();
        UUID recipientUUID = recipient.getUniqueId();

        // Get the LNBitsClient instance
        LNBitsClient client = SCE.getClient();

        // Get cached wallet data for both sender and recipient
        LNBitsUserCache senderCache = client.cacheUsers().get(senderUUID);
        LNBitsUserCache recipientCache = client.cacheUsers().get(recipientUUID);

        // Ensure both users have LNBits wallet data cached
        if (senderCache == null || recipientCache == null) {
            player.sendMessage("§cCould not locate wallets.");
            return true;
        }

        // === STEP 1: Create an invoice on the recipient's wallet ===
        String invoice = client.payments().createInvoice(
            recipientCache.inkey(),                     // Recipient's invoice key
            amount,                                     // Amount in satoshis
            "Payment from " + player.getName()          // Optional memo
        );

        // === STEP 2: Pay the invoice from the sender's wallet ===
        boolean success = client.payments().payInvoice(
            senderCache.adminkey(),                     // Sender's admin key
            invoice                                     // BOLT11 invoice to pay
        );

        // Notify players of the result
        if (success) {
            player.sendMessage("§aSent " + amount + " sats to " + recipientName);
            recipient.sendMessage("§aYou received " + amount + " sats from " + player.getName());
        } else {
            player.sendMessage("§cFailed to send payment.");
        }

        return true;
    }
}
