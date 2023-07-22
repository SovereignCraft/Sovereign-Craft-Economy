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
				//System.out.println("no wallet detected for " + player.getName() + " - creating");
				new BukkitRunnable() {

					@Override
					public void run() {
						SCEconomy.getEco().createAccount(player.getUniqueId());
					}
				}.runTaskAsynchronously(SCEconomy.getInstance());
				player.sendMessage("Your wallet is being created. Use the command /webwallet to access it directly.");
				player.sendMessage("To sync your wallet to your device add the LNDHub extension to your webwallet, click the extension & follow the LNDHub instructions in the web portal");
			}
			else {
				player.sendMessage("You already appear to have a Wallet");
			}

			}else {
			sender.sendMessage("Too many arguments");

			return true;

		}

		return true;
	}
}