package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.LNBits;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.List;

public class LNCommand implements org.bukkit.command.CommandExecutor {

    private final SCEconomy plugin;
    private final LNBits lnbits;

    public LNCommand(SCEconomy plugin) {
        this.plugin = plugin;
        this.lnbits = plugin.getLnbits();
    }

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
            sender.sendMessage("§cYou must specify an ln command");
            return true;
        }

        Player player = (Player) sender;
        String subcommand = args[0].toLowerCase();

        if (!lnbits.hasAccount(player.getUniqueId())) {
            sender.sendMessage("§cYour wallet isn't working");
            return true;
        }

        switch (subcommand) {
            case "bal":
                if (args.length == 1) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            sender.sendMessage("§eYour balance is: §a" + lnbits.getBalanceString(player.getUniqueId()));
                        }
                    }.runTaskAsynchronously(plugin);

                } else if (args.length == 2) {
                    String currency = args[1].toUpperCase();
                    List<String> options = lnbits.getCurrencies();
                    if (options.contains(currency)) {
                        double bal = lnbits.getConversion("sat", lnbits.getBalance(player.getUniqueId()), currency);
                        sender.sendMessage("§eYour balance is: §a" + lnbits.numberFiatFormat(bal) + " " + currency);
                    } else {
                        sender.sendMessage("§c" + currency + " currency not found.");
                    }
                } else {
                    sender.sendMessage("§cUsage: /ln bal [currency]");
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
                FileConfiguration config = plugin.getConfig();
                File configFile = new File(plugin.getDataFolder() + File.separator + "config.yml");
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
                    sender.sendMessage("§cUsage: /ln deposit <amount> [currency]");
                    return true;
                }
                double depositAmount;
                try {
                    depositAmount = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cThis amount is invalid");
                    return true;
                }
                if (depositAmount <= 0) {
                    sender.sendMessage("§cyou cannot use this command to withdraw");
                    return true;
                }
                Double sats = depositAmount;
                if (args.length == 3) {

                    String currency = args[2].toUpperCase();
                    if (!lnbits.getCurrencies().contains(currency)) {
                        sender.sendMessage("§c" + currency + " currency not found.");
                        return true;
                    }
                    sats = lnbits.getConversion(currency, depositAmount, "sats");
                }
                Double finalSats = sats;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        String depositData = lnbits.createInvoice(player.getUniqueId(), finalSats);
                        SCEconomy.playerQRInterface.put(player.getUniqueId(), depositData);
                    }
                }.runTaskAsynchronously(plugin);
                return true;

            case "withdraw":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /ln withdraw <amount> [currency]");
                    return true;
                }
                double withdrawAmount;
                try {
                    withdrawAmount = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cThis amount is invalid");
                    return true;
                }
                if (withdrawAmount <= 0) {
                    sender.sendMessage("§cyou cannot use this command to deposit");
                    return true;
                }
                double withdrawSats = withdrawAmount;
                if (args.length == 3) {
                    String currency = args[2].toUpperCase();
                    if (!lnbits.getCurrencies().contains(currency)) {
                        sender.sendMessage("§c" + currency + " currency not found.");
                        return true;
                    }
                    withdrawSats = lnbits.getConversion(currency, withdrawAmount, "sats");
                }
                String withdrawData = lnbits.createlnurlw(player.getUniqueId(), withdrawSats);
                SCEconomy.playerQRInterface.put(player.getUniqueId(), withdrawData);
                return true;

            case "pay":
                if (args.length != 3) {
                    sender.sendMessage("§cUsage: /ln pay <player> <amount>");
                    return true;
                }
                OfflinePlayer recipient = Bukkit.getOfflinePlayer(args[1]);
                if (recipient == null || recipient.getUniqueId().equals(player.getUniqueId())) {
                    sender.sendMessage("§cInvalid recipient.");
                    return true;
                }
                if (!recipient.hasPlayedBefore()) {
                    sender.sendMessage("§cThis person has never played on this server");
                    return true;
                }
                double payAmount;
                try {
                    payAmount = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cThis amount is invalid");
                    return true;
                }
                if (payAmount <= 0 || !lnbits.has(player.getUniqueId(), payAmount)) {
                    sender.sendMessage("§cInvalid or insufficient funds");
                    return true;
                }
                lnbits.withdraw(player.getUniqueId(), payAmount);
                lnbits.deposit(recipient.getUniqueId(), payAmount);
                sender.sendMessage("§aYou paid " + recipient.getName() + " §e⚡" + lnbits.numberFormat(payAmount));
                if (recipient instanceof Player) {
                    ((Player) recipient).sendMessage("§aYou received §e⚡" + lnbits.numberFormat(payAmount) + " from " + player.getName());
                }
                return true;

            case "qrwebwallet":
                String webwallet = "https://wallet.sovereigncraft.com/wallet?usr=" + lnbits.getUser(player.getUniqueId()).get("id");
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
                player.sendMessage("§aThis command is temporarily disabled");
                return true;

            case "syncwallet":
                lnbits.extension(player.getUniqueId(), "lndhub", true);
                String syncData = "lndhub://admin:" + lnbits.getWalletAdminKey(player.getUniqueId()) + "@https://" + plugin.getConfig().getString("pubhost") + "/lndhub/ext/";
                SCEconomy.playerQRInterface.put(player.getUniqueId(), syncData);
                sender.sendMessage(prefix + SCEconomy.getMessage("messages.success"));
                return true;

            case "webwallet":
                String url = "https://wallet.sovereigncraft.com/wallet?usr=" + lnbits.getUser(player.getUniqueId()).get("id");
                Bukkit.getServer().dispatchCommand(
                        Bukkit.getConsoleSender(),
                        "tellraw " + player.getName() +
                                " {\"text\":\"§aClick here for your web wallet\"," +
                                "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url + "\"}}"
                );
                return true;

            case "send":
                if (args.length == 4 && args[1].equalsIgnoreCase("lnaddress")) {
                    double sendAmount;
                    try {
                        sendAmount = Double.parseDouble(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cThis amount is invalid");
                        return true;
                    }
                    if (sendAmount <= 0 || !lnbits.has(player.getUniqueId(), sendAmount)) {
                        sender.sendMessage("§cStack more Sats");
                        return true;
                    }
                    lnbits.sendLNAddress(player.getUniqueId(), args[2], sendAmount);
                    return true;
                }
                break;

            default:
                sender.sendMessage("§cCommand not found");
                return true;
        }

        return true;
    }
}
