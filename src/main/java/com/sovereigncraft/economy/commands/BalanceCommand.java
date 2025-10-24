package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class BalanceCommand implements org.bukkit.command.CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
				if (args.length == 0) {
				
				if (!(sender instanceof Player)) {
					sender.sendMessage( "§cOnly players can get their balance");
					return true;
				}
				
				Player player = (Player) sender;
				
				if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
					player.sendMessage("§cYour wallet is not working.");
					return true;
				}
				sender.sendMessage(" §eYour balance is: §a" + SCEconomy.getEco().getBalanceString(player.getUniqueId()));
				return true;
				
			}
			else {
				sender.sendMessage("§cToo many arguments");
				
				return true;
				
			}
			

	
	}
	
}
