package com.sovereigncraft.economy.listeners;

import com.sovereigncraft.economy.SCEconomy;
import com.sovereigncraft.economy.ConfigHandler;
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
			player.sendMessage("Your ⚡ wallet is being created.");
			new BukkitRunnable() {

				@Override
				public void run() {
					SCEconomy.getEco().createAccount(player.getUniqueId());
					if (SCEconomy.playerAdminKey.containsKey(player.getUniqueId())){
						SCEconomy.playerAdminKey.remove(player.getUniqueId());
					}
					if (SCEconomy.playerInKey.containsKey(player.getUniqueId())){
						SCEconomy.playerInKey.remove(player.getUniqueId());
					}
				}
			}.runTaskAsynchronously(SCEconomy.getInstance());

		}
		if (SCEconomy.playerAdminKey.containsKey(player.getUniqueId())){
			SCEconomy.playerAdminKey.remove(player.getUniqueId());
		}
		if (SCEconomy.playerInKey.containsKey(player.getUniqueId())){
			SCEconomy.playerInKey.remove(player.getUniqueId());
		}
		if (ConfigHandler.getLNAddress().equals(true)) {
			SCEconomy.getEco().createlnurlp(player.getUniqueId(), "SCLNAddress", 10, 5000000, "SCLNAddress", player.getName());
		}
	}
	
}
