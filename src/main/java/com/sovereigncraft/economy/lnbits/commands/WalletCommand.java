package com.sovereigncraft.economy.lnbits.commands;

import com.sovereigncraft.economy.lnbits.LNBitsClient;
import com.sovereigncraft.economy.LogHandler;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Bukkit command executor for the {@code /wallet} command.
 * <p>
 * This command displays the LNBits wallets associated with the executing player.
 * If no wallet exists, one is automatically created.
 * <p>
 * All actions are logged in JSON format via {@link LogHandler}.
 */
public class WalletCommand implements CommandExecutor {

    private final LNBitsClient client;

    /**
     * Constructs the WalletCommand with the provided LNBits client.
     *
     * @param client The LNBits API client used for user and wallet operations.
     */
    public WalletCommand(LNBitsClient client) {
        this.client = client;
    }

    /**
     * Handles execution of the {@code /wallet} command.
     * <p>
     * If the executing sender is a player, the plugin checks for an existing wallet.
     * If none exists, a new one is created. Then all available wallets are listed.
     * All events are logged via {@link LogHandler#logAction(String, Map)}.
     *
     * @param sender  The command sender (must be a player).
     * @param command The executed command instance.
     * @param label   The alias used to invoke the command.
     * @param args    The command arguments (unused).
     * @return {@code true} if the command was handled successfully.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            LogHandler.logAction("wallet_command_rejected", Map.of(
                "sender", sender.getName(),
                "reason", "non-player"
            ));
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        LogHandler.logAction("wallet_command_invoked", Map.of(
            "player", playerName,
            "uuid", uuid.toString()
        ));

        try {
            if (!client.users().userExists(uuid)) {
                player.sendMessage("§eNo wallet found. Creating one...");
                LogHandler.logAction("wallet_creation_attempt", Map.of(
                    "player", playerName,
                    "uuid", uuid.toString()
                ));

                if (!client.users().createUser(uuid)) {
                    player.sendMessage("§cFailed to create wallet.");
                    LogHandler.logAction("wallet_creation_failed", Map.of(
                        "player", playerName,
                        "uuid", uuid.toString()
                    ));
                    return true;
                }

                LogHandler.logAction("wallet_created", Map.of(
                    "player", playerName,
                    "uuid", uuid.toString()
                ));
            }

            Map<String, Object> user = client.users().getUser(uuid);
            String userId = (String) user.get("id");
            List<Map<String, Object>> wallets = client.wallets().getWallets(userId);

            player.sendMessage("§aYour Wallets:");
            List<String> walletSummaries = new ArrayList<>();

            for (Map<String, Object> wallet : wallets) {
                String name = (String) wallet.get("name");
                double balanceSat = ((Number) wallet.get("balance_msat")).doubleValue() / 1000;
                String msg = "§b" + name + ": §f⚡" + String.format("%,.0f", balanceSat) + " sats";
                player.sendMessage(msg);
                walletSummaries.add(name + " (" + balanceSat + " sats)");
            }

            LogHandler.logAction("wallet_listed", Map.of(
                "player", playerName,
                "uuid", uuid.toString(),
                "wallets", walletSummaries
            ));

        } catch (Exception e) {
            player.sendMessage("§cError: " + e.getMessage());
            LogHandler.logAction("wallet_command_error", Map.of(
                "player", playerName,
                "uuid", uuid.toString(),
                "error", e.getMessage()
            ));
            e.printStackTrace();
        }

        return true;
    }
}
