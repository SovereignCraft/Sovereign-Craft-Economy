package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.ConfigHandler;
import com.sovereigncraft.economy.SCEconomy;
import com.sovereigncraft.economy.util.MapCreator;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class LNCommand implements org.bukkit.command.CommandExecutor {

    @SuppressWarnings("deprecation")
    @Override
    @SneakyThrows
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = SCEconomy.getMessage("messages.prefix");

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + SCEconomy.getMessage("messages.onlyForPlayers"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("You must specify an ln command");
            return true;
        }

        Player player = (Player) sender;
        String subcommand = args[0].toLowerCase();

        if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
            sender.sendMessage("Your wallet isn't working");
            return true;
        }

        switch (subcommand) {
            case "bal":
                if (args.length == 1) {
                    sender.sendMessage(" Your balance is: " + SCEconomy.getEco().getBalanceString(player.getUniqueId()));
                } else if (args.length == 2) {
                    String currency = args[1].toUpperCase();
                    List<String> options = SCEconomy.getEco().getCurrencies();
                    if (options.contains(currency)) {
                        double bal = SCEconomy.getEco().getConversion("sat", SCEconomy.getEco().getBalance(player.getUniqueId()), currency);
                        sender.sendMessage(" Your balance is: " + SCEconomy.getEco().numberFiatFormat(bal) + " " + currency);
                    } else {
                        sender.sendMessage(currency + " currency not found.");
                    }
                } else {
                    sender.sendMessage("Usage: /ln bal [currency]");
                }
                return true;

            case "cls":
                SCEconomy.playerQRInterface.remove(player.getUniqueId());
                sender.sendMessage(prefix + SCEconomy.getMessage("messages.success"));
                return true;

            case "screen":
                if (player.getItemInHand().getType() != Material.AIR) {
                    sender.sendMessage(prefix + SCEconomy.getMessage("messages.handNotEmpty"));
                    return true;
                }
                FileConfiguration config = SCEconomy.getInstance().getConfig();
                File configFile = new File(SCEconomy.getInstance().getDataFolder() + File.separator + "config.yml");
                ItemStack map = config.get("interfaceID") == null ? MapCreator.generatePlayerMap(player) : MapCreator.clonePlayerMap();
                if (config.get("interfaceID") == null) {
                    Integer id = ((MapMeta) map.getItemMeta()).getMapId();
                    config.set("interfaceID", id);
                    config.save(configFile);
                }
                player.setItemInHand(map);
                sender.sendMessage(prefix + SCEconomy.getMessage("messages.success"));
                return true;

            case "deposit":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /ln deposit <amount> [currency]");
                    return true;
                }
                double depositAmount;
                try {
                    depositAmount = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("This amount is invalid");
                    return true;
                }
                if (depositAmount <= 0) {
                    sender.sendMessage("you cannot use this command to withdraw");
                    return true;
                }
                double sats = depositAmount;
                if (args.length == 3) {
                    String currency = args[2].toUpperCase();
                    if (!SCEconomy.getEco().getCurrencies().contains(currency)) {
                        sender.sendMessage(currency + " currency not found.");
                        return true;
                    }
                    sats = SCEconomy.getEco().getConversion(currency, depositAmount, "sats");
                }
                String depositData = SCEconomy.getEco().createInvoice(player.getUniqueId(), sats);
                SCEconomy.playerQRInterface.put(player.getUniqueId(), depositData);
                return true;

            case "withdraw":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /ln withdraw <amount> [currency]");
                    return true;
                }
                double withdrawAmount;
                try {
                    withdrawAmount = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("This amount is invalid");
                    return true;
                }
                if (withdrawAmount <= 0) {
                    sender.sendMessage("you cannot use this command to deposit");
                    return true;
                }
                double withdrawSats = withdrawAmount;
                if (args.length == 3) {
                    String currency = args[2].toUpperCase();
                    if (!SCEconomy.getEco().getCurrencies().contains(currency)) {
                        sender.sendMessage(currency + " currency not found.");
                        return true;
                    }
                    withdrawSats = SCEconomy.getEco().getConversion(currency, withdrawAmount, "sats");
                }
                String withdrawData = SCEconomy.getEco().createlnurlw(player.getUniqueId(), withdrawSats);
                SCEconomy.playerQRInterface.put(player.getUniqueId(), withdrawData);
                return true;

            case "pay":
                if (args.length != 3) {
                    sender.sendMessage("Usage: /ln pay <player> <amount>");
                    return true;
                }
                OfflinePlayer recipient = Bukkit.getOfflinePlayer(args[1]);
                if (recipient == null || recipient.getUniqueId().equals(player.getUniqueId())) {
                    sender.sendMessage("Invalid recipient.");
                    return true;
                }
                if (!SCEconomy.getEco().hasAccount(recipient.getUniqueId())) {
                    sender.sendMessage("This person has no wallet on this server");
                    return true;
                }
                double payAmount;
                try {
                    payAmount = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("This amount is invalid");
                    return true;
                }
                if (payAmount <= 0 || !SCEconomy.getEco().has(player.getUniqueId(), payAmount)) {
                    sender.sendMessage("Invalid or insufficient funds");
                    return true;
                }
                SCEconomy.getEco().withdraw(player.getUniqueId(), payAmount);
                SCEconomy.getEco().deposit(recipient.getUniqueId(), payAmount);
                sender.sendMessage("You paid " + recipient.getName() + " ⚡" + SCEconomy.getEco().numberFormat(payAmount));
                if (recipient instanceof Player) {
                    ((Player) recipient).sendMessage("You received ⚡" + SCEconomy.getEco().numberFormat(payAmount) + " from " + player.getName());
                }
                return true;

            case "qrwebwallet":
                String webwallet = "https://wallet.sovereigncraft.com/wallet?usr=" + SCEconomy.getEco().getUser(player.getUniqueId()).get("id");
                SCEconomy.playerQRInterface.put(player.getUniqueId(), webwallet);
                sender.sendMessage(prefix + SCEconomy.getMessage("messages.success"));
                return true;

            case "qrguide":
                SCEconomy.playerQRInterface.put(player.getUniqueId(), "https://sovereigncraft.com/guide/");
                sender.sendMessage(prefix + SCEconomy.getMessage("messages.success"));
                return true;

            case "qrvote":
                SCEconomy.playerQRInterface.put(player.getUniqueId(), "https://sovereigncraft.com/vote");
                sender.sendMessage(prefix + SCEconomy.getMessage("messages.success"));
                return true;

            case "refreshwallet":
                double bal = SCEconomy.getEco().getBalance(player.getUniqueId());
                SCEconomy.getEco().withdraw(player.getUniqueId(), bal);
                SCEconomy.getEco().userDelete(player.getUniqueId());
                if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
                    SCEconomy.getEco().createAccount(player.getUniqueId());
                    player.sendMessage("Your wallet has been recreated. Use the command /webwallet to access the new one and re-sync it to your mobile wallet if required.");
                    player.sendMessage("To sync your wallet to your device add the LNDHub extension to your webwallet, click the extension & follow the LNDHub instructions in the web portal");
                } else {
                    player.sendMessage("Refresh failed");
                }
                SCEconomy.getEco().deposit(player.getUniqueId(), bal);
                return true;

            case "syncwallet":
                SCEconomy.getEco().extension(player.getUniqueId(), "lndhub", true);
                String syncData = "lndhub://admin:" + SCEconomy.getEco().getWalletAdminKey(player.getUniqueId()) + "@https://" + ConfigHandler.getPubHost() + "/lndhub/ext/";
                SCEconomy.playerQRInterface.put(player.getUniqueId(), syncData);
                sender.sendMessage(prefix + SCEconomy.getMessage("messages.success"));
                return true;

            case "webwallet":
                String url = "https://wallet.sovereigncraft.com/wallet?usr=" + SCEconomy.getEco().getUser(player.getUniqueId()).get("id");
                Bukkit.getServer().dispatchCommand(
                        Bukkit.getConsoleSender(),
                        "tellraw " + player.getName() +
                                " {\"text\":\"Click here for your web wallet\"," +
                                "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url + "\"}}"
                );
                return true;

            case "send":
                if (args.length == 4 && args[1].equalsIgnoreCase("lnaddress")) {
                    double sendAmount;
                    try {
                        sendAmount = Double.parseDouble(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("This amount is invalid");
                        return true;
                    }
                    if (sendAmount <= 0 || !SCEconomy.getEco().has(player.getUniqueId(), sendAmount)) {
                        sender.sendMessage("Stack more Sats");
                        return true;
                    }
                    SCEconomy.getEco().sendLNAddress(player, args[2], sendAmount);
                    return true;
                }
                break;

            default:
                sender.sendMessage("Command not found");
                return true;
        }

        return true;
    }
}
