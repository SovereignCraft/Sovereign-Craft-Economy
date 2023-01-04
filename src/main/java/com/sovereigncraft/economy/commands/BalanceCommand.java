package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.ConfigHandler;
import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

import com.sovereigncraft.economy.SCEconomy;

import java.text.DecimalFormat;

public class BalanceCommand implements org.bukkit.command.CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
				if (args.length == 0) {
				
				if (!(sender instanceof Player)) {
					sender.sendMessage( "Only players can get their balance");
					return true;
				}
				
				Player player = (Player) sender;
				
				if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
					player.sendMessage("You have not created your wallet.");
					SCEconomy.getEco().tosMessage(player);
					return true;
				}
				sender.sendMessage(" Your balance is: " + SCEconomy.getEco().getBalanceString(player.getUniqueId()));
				return true;
				
			}
			else {
				sender.sendMessage("Too many arguments");
				
				return true;
				
			}
			

	
	}
	
}
