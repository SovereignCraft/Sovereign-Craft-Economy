package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

//import com.google.common.collect.ImmutableMap; // isn't used.

public class PayCommand implements org.bukkit.command.CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			
			if (args.length == 2) {
			
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player to send money");
				return true;
			}
			
			Player player = (Player) sender;

				if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
					sender.sendMessage("Your wallet isn't working");
					return true;
				}
			
			OfflinePlayer other = Bukkit.getOfflinePlayer(args[0]);
			
			if (other == null) {
				sender.sendMessage("Could not find other player");
				return true;
			}
			
			if (!SCEconomy.getEco().hasAccount(other.getUniqueId())) {
				sender.sendMessage(String.join(",","This person has no wallet on this server"));
				return true;
			}
			
			if (other.getUniqueId().equals(player.getUniqueId())) {
				sender.sendMessage("So, if you pay yourself, nothing happens. HFSP");
				return true;
			}
			
			double amount = 0;
			try {
				amount = Double.parseDouble(args[1]);
			}
			catch (NumberFormatException e){
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
				sender.sendMessage("You paid " + other.getName() + " ⚡" + SCEconomy.getEco().numberFormat(amount) );
				SCEconomy.getEco().deposit(other.getUniqueId(), amount);
				((Player) other).sendMessage("You received" + " ⚡" + SCEconomy.getEco().numberFormat(amount) + " from " + player.getName());
			}
			
			return true;
			
			}
			else {

				sender.sendMessage("That's not how you do it. You did something wrong");

				return true;

			}
			



	}
	
}
