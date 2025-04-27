package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.ConfigHandler;
import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SyncWallet implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String prefix = SCEconomy.getMessage("messages.prefix");

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix+SCEconomy.getMessage("messages.onlyForPlayers"));
            return true;
        }

        if (args.length > 0) {
            return false;
        }

        Player player = (Player) sender;
        SCEconomy.getEco().extension("LNDHub",true);
        String data = "lndhub://admin:" + SCEconomy.getEco().getWalletAdminKey(player.getUniqueId()) + "@https://" + ConfigHandler.getPubHost() + "/lndhub/ext/";
        if (SCEconomy.playerQRInterface.get(player.getUniqueId()) == null) {
            SCEconomy.playerQRInterface.put(player.getUniqueId(), data);
        } else SCEconomy.playerQRInterface.replace(player.getUniqueId(), data);

        sender.sendMessage(prefix+SCEconomy.getMessage("messages.success"));

        return true;
    }

}
