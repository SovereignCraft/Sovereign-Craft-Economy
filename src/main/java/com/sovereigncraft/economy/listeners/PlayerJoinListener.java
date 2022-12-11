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
		
		if (!SCEconomy.getEco().hasAccount(player.getUniqueId())) {
			new BukkitRunnable() {

				@Override
				public void run() {
					SCEconomy.getEco().createAccount(player.getUniqueId());
				}
			}.runTaskAsynchronously(SCEconomy.getInstance());
		}
		
	}
	
}
