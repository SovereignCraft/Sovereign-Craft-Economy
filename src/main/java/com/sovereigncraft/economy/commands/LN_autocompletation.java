package com.sovereigncraft.economy.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import com.sovereigncraft.economy.SCEconomy;

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
                options = Arrays.asList("screen", "deposit", "withdraw", "send", "cls", "webwallet", "bal", "pay", "qrvote", "qrguide", "qrwebwallet", "syncwallet", "refreshwallet");
                List<String> filteredOptions = new ArrayList<>();
                for (String option : options) {
                    if (option.startsWith(input))
                        filteredOptions.add(option);
                }
                return filteredOptions;
            }
            if (args.length == 2) {
                if (args[0].equals("bal")) {
                    String input = args[1].toUpperCase();
                    List<String> options = SCEconomy.getEco().getCurrencies();
                    List<String> filteredOptions = new ArrayList<>();
                    for (Object object : options) {
                        String option = object.toString();
                        if (option.startsWith(input) && !(input.startsWith("[")))
                            filteredOptions.add(option);
                    }
                    return filteredOptions;
                }
            }
            if (args.length == 2) {
                if (args[0].equals("send")) {
                    String input = args[1].toLowerCase();
                    List<String> filteredOptions = new ArrayList<>();
                    List<String> options = Arrays.asList("lnaddress");
                    for (String option : options) {
                        if (option.startsWith(input))
                            filteredOptions.add(option);
                    }
                    return filteredOptions;
                }
            }
            if (args.length == 3) {
                if (args[0].equals("send") && args[1].equals("lnaddress")) {
                    List<String> filteredOptions = new ArrayList<>();
                    if (args[2].isEmpty()) {
                        List<String> options;
                        options = Arrays.asList("[lnaddress (someone@somewhere.com)]");
                        filteredOptions.addAll(options);
                    }
                    return filteredOptions;
                }
            }
            if (args.length == 4) {
                if (args[0].equals("send") && args[1].equals("lnaddress")) {
                    List<String> filteredOptions = new ArrayList<>();
                    if (args[3].isEmpty()) {
                        List<String> options;
                        options = Arrays.asList("[amount]");
                        filteredOptions.addAll(options);
                    }
                    return filteredOptions;
                }
            }
            if (args.length == 2) {
                if (args[0].equals("deposit") || args[0].equals("withdraw") || args[0].equals("pay")) {
                    List<String> filteredOptions = new ArrayList<>();
                    if (args[1].isEmpty()) {
                        List<String> options = Arrays.asList("[Enter the amount]");
                        filteredOptions.addAll(options);
                    }
                    return filteredOptions;
                }
            }
            if (args.length == 3) {
                if (args[0].equals("deposit") || args[0].equals("withdraw")) {
                    String input = args[2].toUpperCase();
                    List<String> options = SCEconomy.getEco().getCurrencies();
                    List<String> filteredOptions = new ArrayList<>();
                    for (Object object : options) {
                        String option = object.toString();
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