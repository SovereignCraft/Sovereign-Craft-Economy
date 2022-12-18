package com.sovereigncraft.economy.listeners;

import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();
		System.out.println("Player joined and event ran");
		System.out.println(player.getUniqueId());
		//SCEconomy.getEco().createAccount(player.getUniqueId());
		//async not working
		if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
			System.out.println("no wallet detected");
			new BukkitRunnable() {

				@Override
				public void run() {
					SCEconomy.getEco().createAccount(player.getUniqueId());
				}
			}.runTaskAsynchronously(SCEconomy.getInstance());
		}
		/*if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
			System.out.println("no wallet detected");
			SCEconomy.getEco().createAccount(player.getUniqueId());
		}*/

	}
	
}
