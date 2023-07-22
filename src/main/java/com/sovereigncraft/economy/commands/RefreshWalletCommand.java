package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RefreshWalletCommand implements org.bukkit.command.CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
				if (args.length == 0) {
				
				if (!(sender instanceof Player)) {
					sender.sendMessage( "Only players can refresh their wallet");
					return true;
				}
				
				Player player = (Player) sender;
				
				if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
					player.sendMessage("You have not created your wallet.");
					sender.sendMessage("Your wallet isn't working");
					return true;
				}
				Double bal = SCEconomy.getEco().getBalance(player.getUniqueId());
				SCEconomy.getEco().withdraw(player.getUniqueId(),bal);
				SCEconomy.getEco().userDelete(player.getUniqueId());
				if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
					SCEconomy.getEco().createAccount(player.getUniqueId());
					player.sendMessage("Your wallet has been recreated. Use the command /webwallet to access the new one and re-sync it to your mobile wallet wf required.");
					player.sendMessage("To sync your wallet to your device add the LNDHub extension to your webwallet, click the extension & follow the LNDHub instructions in the web portal");
				}
				else {
					player.sendMessage("Refresh failed");
				}
				SCEconomy.getEco().deposit(player.getUniqueId(),bal);
				return true;
				
			}
			else {
				sender.sendMessage("Too many arguments");
				
				return true;
				
			}
			

	
	}
	
}
