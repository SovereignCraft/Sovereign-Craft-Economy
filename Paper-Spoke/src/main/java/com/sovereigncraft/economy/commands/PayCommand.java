package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.PaperSpoke;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {

    private final PaperSpoke plugin;
    private final Economy economy;

    public PayCommand(PaperSpoke plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 2) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must be a player to send money");
                return true;
            }

            Player player = (Player) sender;

            if (!economy.hasAccount(player)) {
                sender.sendMessage("§cYou don't have an account.");
                return true;
            }

            OfflinePlayer other = Bukkit.getOfflinePlayer(args[0]);

            if (other == null) {
                sender.sendMessage("§cCould not find other player");
                return true;
            }

            if (!economy.hasAccount(other)) {
                sender.sendMessage("§cThis person has no wallet on this server");
                return true;
            }

            if (other.getUniqueId().equals(player.getUniqueId())) {
                sender.sendMessage("§cYou can't pay yourself.");
                return true;
            }

            double amount;
            try {
                amount = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cThis amount is invalid");
                return true;
            }
            if (amount <= 0) {
                sender.sendMessage("§cYou must pay a positive amount.");
                return true;
            }

            if (!economy.has(player, amount)) {
                sender.sendMessage("§cYou don't have enough funds.");
                return true;
            }

            EconomyResponse withdrawResponse = economy.withdrawPlayer(player, amount);
            if (withdrawResponse.type == EconomyResponse.ResponseType.SUCCESS) {
                EconomyResponse depositResponse = economy.depositPlayer(other, amount);
                if (depositResponse.type == EconomyResponse.ResponseType.SUCCESS) {
                    sender.sendMessage("§eYou paid " + other.getName() + " §a" + economy.format(amount));
                    if (other.isOnline()) {
                        ((Player) other).sendMessage("§eYou received" + " §a" + economy.format(amount) + " from " + player.getName());
                    }
                } else {
                    // Refund the player if the deposit fails
                    economy.depositPlayer(player, amount);
                    sender.sendMessage("§cPayment failed. " + depositResponse.errorMessage);
                }
            } else {
                sender.sendMessage("§cPayment failed. " + withdrawResponse.errorMessage);
            }

            return true;

        } else {
            sender.sendMessage("§cUsage: /pay <player> <amount>");
            return true;
        }
    }
}