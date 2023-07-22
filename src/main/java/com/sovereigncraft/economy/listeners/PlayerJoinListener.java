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
			player.sendMessage("Your ⚡ wallet is being created Type /tos to view our terms of service.");
			new BukkitRunnable() {

				@Override
				public void run() {
					SCEconomy.getEco().createAccount(player.getUniqueId());
				}
			}.runTaskAsynchronously(SCEconomy.getInstance());
		} else {
			player.sendMessage("To deposit/use your ⚡ wallet in real life type /webwallet.");
			player.sendMessage("To sync your wallet to your device add the LNDHub extension to your webwallet, click the extension & follow the LNDHub instructions in the web portal");
		}
		SCEconomy.getEco().createlnurlp(player.getUniqueId(), "SCLNAddress", 10 , 5000000, "SCLNAddress", player.getName());

	}
	
}
