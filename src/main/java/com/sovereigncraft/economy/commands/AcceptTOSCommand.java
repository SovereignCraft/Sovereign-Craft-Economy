package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AcceptTOSCommand implements org.bukkit.command.CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (args.length == 0) {

			if (!(sender instanceof Player)) {
				sender.sendMessage("Only players can create a wallet");
				return true;
			}
			Player player = (Player) sender;
			if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
				System.out.println("no wallet detected for " + player.getName() + " - creating");
				new BukkitRunnable() {

					@Override
					public void run() {
						SCEconomy.getEco().createAccount(player.getUniqueId());
					}
				}.runTaskAsynchronously(SCEconomy.getInstance());

			} else {
				sender.sendMessage("Too many arguments");

				return true;

			}
		/*if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
			System.out.println("no wallet detected");
			SCEconomy.getEco().createAccount(player.getUniqueId());
		}*/


		}

		return true;
	}
}