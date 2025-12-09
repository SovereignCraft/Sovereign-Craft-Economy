package com.sovereigncraft.economy.listeners;

import com.sovereigncraft.economy.ConfigHandler;
import com.sovereigncraft.economy.LNBits;
import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        LNBits lnbits = SCEconomy.getEco();

        new BukkitRunnable() {
            @Override
            public void run() {
                // Clear any cached keys on join

                if (SCEconomy.playerAdminKey.containsKey(player.getUniqueId())) {
                    SCEconomy.playerAdminKey.remove(player.getUniqueId());
                }
                if (SCEconomy.playerInKey.containsKey(player.getUniqueId())) {
                    SCEconomy.playerInKey.remove(player.getUniqueId());
                }
                //cache the keys
                lnbits.getWalletAdminKey(player.getUniqueId());
                lnbits.getWalletinkey(player.getUniqueId());
                // Create LNURL-Pay link if enabled
                if (ConfigHandler.getLNAddress()) {
                    lnbits.createlnurlp(player, "SCLNAddress", 10, 5000000, "SCLNAddress");
                }
            }
        }.runTaskAsynchronously(SCEconomy.getInstance());
    }
}