package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.PaperSpoke;
import com.sovereigncraft.economy.util.MapCreator;
import com.sovereigncraft.economy.util.QRCreator;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ExecutionException;

public class LNCommand implements CommandExecutor {

    private final PaperSpoke plugin;
    private final Economy economy;
    private final QRCreator qrCreator;

    public LNCommand(PaperSpoke plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
        this.qrCreator = new QRCreator(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cYou must specify an ln command");
            return true;
        }

        Player player = (Player) sender;
        String subcommand = args[0].toLowerCase();

        if (!economy.hasAccount(player)) {
            sender.sendMessage("§cYou don't have an account.");
            return true;
        }

        switch (subcommand) {
            case "bal":
                if (args.length == 1) {
                    sender.sendMessage("§eYour balance is: §a" + economy.format(economy.getBalance(player)));
                } else {
                    sender.sendMessage("§cUsage: /ln bal");
                }
                return true;
            case "deposit":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /ln deposit <amount>");
                    return true;
                }
                double depositAmount;
                try {
                    depositAmount = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid amount.");
                    return true;
                }
                if (depositAmount <= 0) {
                    sender.sendMessage("§cAmount must be positive.");
                    return true;
                }
                try {
                    String invoice = plugin.createInvoice(player, depositAmount).get();
                    plugin.playerQRInterface.put(player.getUniqueId(), invoice);
                    sender.sendMessage("§aQR code for deposit generated. Use /ln screen to view it.");
                } catch (InterruptedException | ExecutionException e) {
                    sender.sendMessage("§cFailed to create invoice.");
                    e.printStackTrace();
                }
                return true;
            case "withdraw":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /ln withdraw <amount>");
                    return true;
                }
                double withdrawAmount;
                try {
                    withdrawAmount = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid amount.");
                    return true;
                }
                if (withdrawAmount <= 0) {
                    sender.sendMessage("§cAmount must be positive.");
                    return true;
                }
                if (!economy.has(player, withdrawAmount)) {
                    sender.sendMessage("§cInsufficient funds.");
                    return true;
                }
                try {
                    String lnurlw = plugin.createLNURLWithdraw(player, withdrawAmount).get();
                    plugin.playerQRInterface.put(player.getUniqueId(), lnurlw);
                    sender.sendMessage("§aQR code for withdrawal generated. Use /ln screen to view it.");
                } catch (InterruptedException | ExecutionException e) {
                    sender.sendMessage("§cFailed to create withdrawal.");
                    e.printStackTrace();
                }
                return true;
            case "screen":
                if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                    sender.sendMessage("§cPlease empty your main hand before getting a screen.");
                    return true;
                }
                ItemStack map = MapCreator.generatePlayerMap(plugin, player);
                player.getInventory().setItemInMainHand(map);
                sender.sendMessage("§aYou have been given a screen.");
                return true;
            case "cls":
                plugin.playerQRInterface.remove(player.getUniqueId());
                sender.sendMessage("§aScreen cleared.");
                return true;
            case "qrwebwallet":
                try {
                    String userId = plugin.getUserId(player).get();
                    String webwallet = "https://wallet.sovereigncraft.com/wallet?usr=" + userId;
                    plugin.playerQRInterface.put(player.getUniqueId(), webwallet);
                    sender.sendMessage("§aWeb wallet QR code generated. Use /ln screen to view it.");
                } catch (InterruptedException | ExecutionException e) {
                    sender.sendMessage("§cFailed to get user ID.");
                    e.printStackTrace();
                }
                return true;
            case "qrguide":
                plugin.playerQRInterface.put(player.getUniqueId(), "https://sovereigncraft.com/guide/");
                sender.sendMessage("§aGuide QR code generated. Use /ln screen to view it.");
                return true;
            case "qrvote":
                plugin.playerQRInterface.put(player.getUniqueId(), "https://sovereigncraft.com/vote");
                sender.sendMessage("§aVote QR code generated. Use /ln screen to view it.");
                return true;
            case "webwallet":
                try {
                    String userId = plugin.getUserId(player).get();
                    String url = "https://wallet.sovereigncraft.com/wallet?usr=" + userId;
                    Bukkit.getServer().dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "tellraw " + player.getName() +
                                    " {\"text\":\"§aClick here for your web wallet\"," +
                                    "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url + "\"}}"
                    );
                } catch (InterruptedException | ExecutionException e) {
                    sender.sendMessage("§cFailed to get user ID.");
                    e.printStackTrace();
                }
                return true;
            case "syncwallet":
                try {
                    String adminKey = plugin.getWalletAdminKey(player).get();
                    String pubhost = plugin.getConfig().getString("pubhost", "localhost"); // Placeholder
                    String syncData = "lndhub://admin:" + adminKey + "@https://" + pubhost + "/lndhub/ext/";
                    plugin.playerQRInterface.put(player.getUniqueId(), syncData);
                    sender.sendMessage("§aSync QR code generated. Use /ln screen to view it.");
                } catch (InterruptedException | ExecutionException e) {
                    sender.sendMessage("§cFailed to get wallet admin key.");
                    e.printStackTrace();
                }
                return true;
            case "send":
                if (args.length != 4 || !args[1].equalsIgnoreCase("lnaddress")) {
                    sender.sendMessage("§cUsage: /ln send lnaddress <address> <amount>");
                    return true;
                }
                double sendAmount;
                try {
                    sendAmount = Double.parseDouble(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid amount.");
                    return true;
                }
                if (sendAmount <= 0) {
                    sender.sendMessage("§cAmount must be positive.");
                    return true;
                }
                if (!economy.has(player, sendAmount)) {
                    sender.sendMessage("§cInsufficient funds.");
                    return true;
                }
                try {
                    boolean success = plugin.sendLNAddress(player, args[2], sendAmount).get();
                    if (success) {
                        sender.sendMessage("§aPayment successful.");
                    } else {
                        sender.sendMessage("§cPayment failed.");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    sender.sendMessage("§cAn error occurred while sending the payment.");
                    e.printStackTrace();
                }
                return true;
            default:
                sender.sendMessage("§cUnknown ln command.");
                return true;
        }
    }
}