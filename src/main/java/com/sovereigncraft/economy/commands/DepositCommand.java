package com.sovereigncraft.economy.commands;

import com.google.common.collect.ImmutableMap;
import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DepositCommand implements org.bukkit.command.CommandExecutor {

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 1) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("You must be a player to deposit money");
                return true;
            }

            Player cur_player = (Player) sender;

            if (!SCEconomy.getEco().hasAccount(cur_player.getUniqueId())) {
                SCEconomy.getEco().tosMessage(cur_player);
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
                sender.sendMessage("you cannot use this command to withdraw");
                return true;
            }
            else {
                String invoice = SCEconomy.getEco().createInvoice(cur_player.getUniqueId(), amount);
                System.out.println(invoice);
                cur_player.chat("/qrcode " + invoice);
                }

            return true;

        } else {

            sender.sendMessage("That's not how you do it. You did something wrong");

            return true;

        }
    }



    }
