package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TOSCommand implements org.bukkit.command.CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage( "Only players can get their balance");
				return true;
			}

			Player player = (Player) sender;
			SCEconomy.getEco().tosMessage(player);
			} else {
				sender.sendMessage("Too many arguments");

			}


		return true;
	}
}