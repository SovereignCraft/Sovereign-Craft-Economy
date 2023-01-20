package com.sovereigncraft.economy.commands;

import com.google.common.collect.ImmutableMap;
import com.sovereigncraft.economy.SCEconomy;

import org.bukkit.command.Command;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DonateCommand implements org.bukkit.command.CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You must be a player to donate");
                return true;
            }
            Player player = (Player) sender;
            if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
                SCEconomy.getEco().tosMessage(player);
                return true;
            }
            double amount = 0;
            try {
                amount = Double.parseDouble(args[0]);
            }
            catch (NumberFormatException e){
                sender.sendMessage("This amount is invalid");
                return true;
            }
            if (amount <= 0) {
                sender.sendMessage("Paying a negative amount is almost like stealing. No, just, no.");
                return true;
            }
            if (!SCEconomy.getEco().has(player.getUniqueId(), amount)) {
                sender.sendMessage("Stack more Sats");
                return true;
            }
            System.out.println("made it to the command");
            SCEconomy.getEco().withdraw(player.getUniqueId(), amount);
            System.out.println("now past withdraw");
            sender.sendMessage("You donated " + ImmutableMap.of("%amount%", amount) + " thank you so much!");
            return true;
        } else{
            return true;
        }
    }
    /* @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> results = new ArrayList<>();
        if (args.length == 1) {
            results.add("<Amount>");
        }
        return results;
    }*/
}
