package com.sovereigncraft.economy.listeners;

import com.sovereigncraft.economy.LNBits;
import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

    private final SCEconomy plugin;
    private final LNBits lnbits;

    public PlayerJoinListener(SCEconomy plugin) {
        this.plugin = plugin;
        this.lnbits = plugin.getLnbits();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                // Clear any cached keys on join
                lnbits.clearPlayerCache(player.getUniqueId());
                //cache the keys
                lnbits.getWalletAdminKey(player.getUniqueId());
                lnbits.getWalletinkey(player.getUniqueId());
                // Create LNURL-Pay link if enabled
                if (plugin.getConfig().getBoolean("lnaddress")) {
                    lnbits.createlnurlp(player.getUniqueId(), "SCLNAddress", 10, 5000000, "SCLNAddress", player.getName());
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}
