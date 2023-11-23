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
        if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
            sender.sendMessage("Your wallet isn't working");
            return true;
        }
        if (Objects.equals(args[0], "bal") && args.length == 1) {
        sender.sendMessage(" Your balance is: " + SCEconomy.getEco().getBalanceString(player.getUniqueId()));
        return true;
    }
        if (Objects.equals(args[0], "cls") && args.length == 1) {
            String data = "";
            if (SCEconomy.playerQRInterface.get(player.getUniqueId()) == null) {
            } else SCEconomy.playerQRInterface.remove(player.getUniqueId());

            sender.sendMessage(prefix + SCEconomy.getMessage("messages.success"));

            return true;
        }
        if (Objects.equals(args[0], "screen") && args.length == 1) {
            if (player.getItemInHand().getType() != Material.AIR) {
                sender.sendMessage(prefix + SCEconomy.getMessage("messages.handNotEmpty"));
                return true;
            }
            if (SCEconomy.getInstance().getConfig().get("interfaceID") == null) {
                ItemStack map = MapCreator.generatePlayerMap(player);
                Integer id = ((MapMeta) map.getItemMeta()).getMapId();
                player.setItemInHand(map);
                File configFile = new File(SCEconomy.getInstance().getDataFolder() + File.separator + "config.yml");
                FileConfiguration config = SCEconomy.getInstance().getConfig();
                config.set("interfaceID", id);
                config.save(configFile);

            } else {
                ItemStack map = MapCreator.clonePlayerMap();
                player.setItemInHand(map);
            }
            sender.sendMessage(prefix + SCEconomy.getMessage("messages.success"));
            return true;
        }
        if (Objects.equals(args[0], "deposit") && args.length == 2) {

            double amount = 0;
            try {
                amount = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("This amount is invalid");
                return true;
            }
            if (amount <= 0) {
                sender.sendMessage("you cannot use this command to withdraw");
                return true;
            } else {
                String data = SCEconomy.getEco().createInvoice(player.getUniqueId(), amount);
                if (SCEconomy.playerQRInterface.get(player.getUniqueId()) == null) {
                    SCEconomy.playerQRInterface.put(player.getUniqueId(), data);
                } else SCEconomy.playerQRInterface.replace(player.getUniqueId(), data);
                //SCEconomy.playerQRInterface.get(player.getUniqueId());
                //cur_player.chat("/qrcode " + invoice);
            }

            return true;

        }
        if (Objects.equals(args[0], "pay") && args.length == 3) {
            OfflinePlayer other = Bukkit.getOfflinePlayer(args[1]);

            if (other == null) {
                sender.sendMessage("Could not find other player");
                return true;
            }

            if (!SCEconomy.getEco().hasAccount(other.getUniqueId())) {
                sender.sendMessage(String.join(",", "This person has no wallet on this server"));
                return true;
            }

            if (other.getUniqueId().equals(player.getUniqueId())) {
                sender.sendMessage("So, if you pay yourself, nothing happens. HFSP");
                return true;
            }

            double amount = 0;
            try {
                amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
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
            } else if (!(other instanceof Player)) {
                sender.sendMessage("There is no player by that name");
            } else {
                SCEconomy.getEco().withdraw(player.getUniqueId(), amount);
                sender.sendMessage("You paid " + other.getName() + " ⚡" + SCEconomy.getEco().numberFormat(amount));
                SCEconomy.getEco().deposit(other.getUniqueId(), amount);
                ((Player) other).sendMessage("You received" + " ⚡" + SCEconomy.getEco().numberFormat(amount) + " from " + player.getName());
            }

            return true;
        }
        if (Objects.equals(args[0], "qrwebwallet") && args.length == 1) {
            String data = "https://wallet.sovereigncraft.com/wallet?usr=" + SCEconomy.getEco().getUser(player.getUniqueId()).get("id");
            if (SCEconomy.playerQRInterface.get(player.getUniqueId()) == null) {
                SCEconomy.playerQRInterface.put(player.getUniqueId(), data);
            } else SCEconomy.playerQRInterface.replace(player.getUniqueId(), data);


            sender.sendMessage(prefix + SCEconomy.getMessage("messages.success"));

            return true;
        }
        if (Objects.equals(args[0], "refreshwallet") && args.length == 1) {

            Double bal = SCEconomy.getEco().getBalance(player.getUniqueId());
            SCEconomy.getEco().withdraw(player.getUniqueId(), bal);
            SCEconomy.getEco().userDelete(player.getUniqueId());
            if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
                SCEconomy.getEco().createAccount(player.getUniqueId());
                player.sendMessage("Your wallet has been recreated. Use the command /webwallet to access the new one and re-sync it to your mobile wallet wf required.");
                player.sendMessage("To sync your wallet to your device add the LNDHub extension to your webwallet, click the extension & follow the LNDHub instructions in the web portal");
            } else {
                player.sendMessage("Refresh failed");
            }
            SCEconomy.getEco().deposit(player.getUniqueId(), bal);
            return true;
        }
        if (Objects.equals(args[0], "syncwallet") && args.length == 1) {
            SCEconomy.getEco().extension(player.getUniqueId(), "LNDHub", true);
            String data = "lndhub://admin:" + SCEconomy.getEco().getWalletAdminKey(player.getUniqueId()) + "@https://" + ConfigHandler.getPubHost() + "/lndhub/ext/";
            if (SCEconomy.playerQRInterface.get(player.getUniqueId()) == null) {
                SCEconomy.playerQRInterface.put(player.getUniqueId(), data);
            } else SCEconomy.playerQRInterface.replace(player.getUniqueId(), data);

            sender.sendMessage(prefix + SCEconomy.getMessage("messages.success"));

            return true;
        }
        if (Objects.equals(args[0], "webwallet") && args.length == 1) {
            String url = "Https://wallet.sovereigncraft.com/wallet?usr=" + SCEconomy.getEco().getUser(player.getUniqueId()).get("id");
            Bukkit.getServer().dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "tellraw " + player.getName() +
                            " {\"text\":\"" + "Click here for your web wallet" +
                            "\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" +
                            url + "\"}}");
            return true;
        }
        if (Objects.equals(args[0], "withdraw") && args.length == 2) {
            double amount = 0;
            try {
                amount = Double.parseDouble(args[1]);
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

        }
            return true;
    }

    }

