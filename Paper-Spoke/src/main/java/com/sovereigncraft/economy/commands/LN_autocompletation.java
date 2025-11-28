package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.PaperSpoke;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LN_autocompletation implements TabCompleter {

    private final PaperSpoke plugin;

    public LN_autocompletation(PaperSpoke plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("ln")) {

            if (args.length == 1) {
                String input = args[0].toLowerCase();
                List<String> options = Arrays.asList("screen", "deposit", "withdraw", "send", "cls", "webwallet", "bal", "pay", "qrvote", "qrguide", "qrwebwallet", "syncwallet");
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
                    try {
                        List<String> options = plugin.getCurrencies().get();
                        List<String> filteredOptions = new ArrayList<>();
                        for (String option : options) {
                            if (option.startsWith(input))
                                filteredOptions.add(option);
                        }
                        return filteredOptions;
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
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
                        filteredOptions.add("[lnaddress]");
                    }
                    return filteredOptions;
                }
            }
            if (args.length == 4) {
                if (args[0].equals("send") && args[1].equals("lnaddress")) {
                    List<String> filteredOptions = new ArrayList<>();
                    if (args[3].isEmpty()) {
                        filteredOptions.add("[amount]");
                    }
                    return filteredOptions;
                }
            }
            if (args.length == 2) {
                if (args[0].equals("deposit") || args[0].equals("withdraw") || args[0].equals("pay")) {
                    List<String> filteredOptions = new ArrayList<>();
                    if (args[1].isEmpty()) {
                        filteredOptions.add("[amount]");
                    }
                    return filteredOptions;
                }
            }
            if (args.length == 3) {
                if (args[0].equals("deposit") || args[0].equals("withdraw")) {
                    String input = args[2].toUpperCase();
                    try {
                        List<String> options = plugin.getCurrencies().get();
                        List<String> filteredOptions = new ArrayList<>();
                        for (String option : options) {
                            if (option.startsWith(input))
                                filteredOptions.add(option);
                        }
                        return filteredOptions;
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}