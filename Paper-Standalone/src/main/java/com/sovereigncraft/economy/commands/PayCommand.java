package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.LNBits;
import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements org.bukkit.command.CommandExecutor {

    private final SCEconomy plugin;
    private final LNBits lnbits;

    public PayCommand(SCEconomy plugin) {
        this.plugin = plugin;
        this.lnbits = plugin.getLnbits();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 2) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must be a player to send money");
                return true;
            }

            Player player = (Player) sender;

            if (!lnbits.hasAccount(player.getUniqueId())) {
                sender.sendMessage("§cYour wallet isn't working");
                return true;
            }

            OfflinePlayer other = Bukkit.getOfflinePlayer(args[0]);

            if (other == null) {
                sender.sendMessage("§cCould not find other player");
                return true;
            }

            if (!lnbits.hasAccount(other.getUniqueId())) {
                sender.sendMessage(String.join(",", "§cThis person has no wallet on this server"));
                return true;
            }

            if (other.getUniqueId().equals(player.getUniqueId())) {
                sender.sendMessage("§cSo, if you pay yourself, nothing happens. HFSP");
                return true;
            }

            double amount = 0;
            try {
                amount = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cThis amount is invalid");
                return true;
            }
            if (amount <= 0) {
                sender.sendMessage("§cPaying a negative amount is almost like stealing. No, just, no.");
                return true;
            }

            if (!lnbits.has(player.getUniqueId(), amount)) {
                sender.sendMessage("§cStack more Sats");
                return true;
            } else if (!(other instanceof Player)) {
                sender.sendMessage("§cThere is no player by that name");
            } else {
                lnbits.withdraw(player.getUniqueId(), amount);
                sender.sendMessage("§eYou paid " + other.getName() + " §a⚡" + lnbits.numberFormat(amount));
                lnbits.deposit(other.getUniqueId(), amount);
                ((Player) other).sendMessage("§eYou received" + " §a⚡" + lnbits.numberFormat(amount) + " from " + player.getName());
            }

            return true;

        } else {

            sender.sendMessage("That's not how you do it. You did something wrong");

            return true;

        }


    }

}
