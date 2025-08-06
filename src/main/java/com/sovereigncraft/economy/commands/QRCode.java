package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.SCEconomy;
import com.sovereigncraft.economy.util.QRCreator;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QRCode implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String prefix = SCEconomy.getMessage("messages.prefix");

        if (!sender.hasPermission("sceconomy.qrcode")) {
            sender.sendMessage(prefix + "You do not have permission to use this command.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + SCEconomy.getMessage("messages.onlyForPlayers"));
            return true;
        }

        if (args.length == 0) {
            return false;
        }

        Player player = (Player) sender;

        if (player.getItemInHand().getType() != Material.AIR) {
            sender.sendMessage(prefix+SCEconomy.getMessage("messages.handNotEmpty"));
            return true;
        }

        String data = "";
        Integer num = 1;
        Integer max = args.length;
        for (String arg : args) {
            data += arg;
            if (num < max) {
                data += " ";
            }
            num++;
        }

        QRCreator QRCreator = new QRCreator(data);
        QRCreator.generate(data, player);

        sender.sendMessage(prefix+SCEconomy.getMessage("messages.success"));

        return true;
    }

}
