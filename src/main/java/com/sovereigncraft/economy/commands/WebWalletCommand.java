package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class WebWalletCommand implements org.bukkit.command.CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
				if (args.length == 0) {
				
				if (!(sender instanceof Player)) {
					sender.sendMessage( "Only players can get their web wallet");
					return true;
				}

				
				Player player = (Player) sender;
				if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
					SCEconomy.getEco().tosMessage(player);
					return true;
				}
				String url = "Https://wallet.sovereigncraft.com/wallet?usr=" + SCEconomy.getEco().getUser(player.getUniqueId()).get("id");
				System.out.println(url);
					Bukkit.getServer().dispatchCommand(
							Bukkit.getConsoleSender(),
							"tellraw " + player.getName() +
									" {\"text\":\"" + "Click here for your web wallet" +
									"\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" +
									url + "\"}}");
				return true;

			}
			else {
				sender.sendMessage("Too many arguments");

				return true;

			}



	}
	
}
