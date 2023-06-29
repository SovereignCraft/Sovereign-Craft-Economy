package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerQRCode implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String prefix = SCEconomy.getMessage("messages.prefix");

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix+SCEconomy.getMessage("messages.onlyForPlayers"));
            return true;
        }

        if (args.length == 0) {
            return false;
        }

        Player player = (Player) sender;

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
        if (SCEconomy.playerQRInterface.get(player.getUniqueId()) == null) {
            SCEconomy.playerQRInterface.put(player.getUniqueId(), data);
        } else SCEconomy.playerQRInterface.replace(player.getUniqueId(), data);
        SCEconomy.playerQRInterface.get(player.getUniqueId());
        //QRCreator QRCreator = new QRCreator(data);
        //QRCreator.playerGenerate(player);

        sender.sendMessage(prefix+SCEconomy.getMessage("messages.success"));

        return true;
    }

}
