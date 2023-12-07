package com.sovereigncraft.economy.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LN_autocompletation implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("ln")) {

            if (args.length == 1) {
                String input = args[0].toLowerCase();
                List<String> options;
                options = Arrays.asList("screen", "deposit", "withdraw", "cls", "webwallet", "bal", "pay", "qrvote", "qrguide", "qrwebwallet", "qrsyncwallet", "syncwallet", "refreshwallet");
                List<String> filteredOptions = new ArrayList<>();
                for (String option : options) {
                    if (option.startsWith(input))
                        filteredOptions.add(option);
                }
                return filteredOptions;
            }

            if (args.length == 2) {
                if (args[0].equals("deposit") || args[0].equals("withdraw") || args[0].equals("pay")) {
                    String input = args[1].toLowerCase();
                    List<String> options;
                    options = Arrays.asList("[Enter the amount]");
                    List<String> filteredOptions = new ArrayList<>();
                    for (String option : options) {
                        if (option.startsWith(input) && !(input.startsWith("[")))
                            filteredOptions.add(option);
                    }
                    return filteredOptions;
                }
            }
        }
        return null;
    }
}