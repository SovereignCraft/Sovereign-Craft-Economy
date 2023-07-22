package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WithdrawCommand implements org.bukkit.command.CommandExecutor {

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 1) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("You must be a player to withdraw money");
                return true;
            }

            Player player = (Player) sender;

            if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
                sender.sendMessage("Your wallet isn't working");
                return true;
            }

            double amount = 0;
            try {
                amount = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage("This amount is invalid");
                return true;
            }
            if (amount <= 0) {
                sender.sendMessage("you cannot use this command to deposit");
                return true;
            }
            else {
                String data = SCEconomy.getEco().createlnurlw(player.getUniqueId(), amount);
                if (SCEconomy.playerQRInterface.get(player.getUniqueId()) == null) {
                    SCEconomy.playerQRInterface.put(player.getUniqueId(), data);
                } else SCEconomy.playerQRInterface.replace(player.getUniqueId(), data);
                }

            return true;

        } else {

            sender.sendMessage("That's not how you do it. You did something wrong");

            return true;

        }
    }



    }
