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
                // Check if a user exists in the new V1 API with the player's UUID as external_id
                Map<String, Object> userV1 = lnbits.getUserV1ByExternalId(player.getUniqueId());

                if (userV1 == null) {
                    // No user found in V1, check the old usermanager API
                    Map<String, Object> oldUser = lnbits.getUser(player.getUniqueId());

                    if (oldUser != null) {
                        // User found in old system, migrate by updating with external_id
                        player.sendMessage("§eFound your existing ⚡ wallet, migrating to the new system...");
                        String walletId = (String) oldUser.get("id");
                        try {
                            lnbits.updateUserWithDefaults(walletId, player);
                            player.sendMessage("§aWallet migration successful!");
                        } catch (RuntimeException e) {
                            player.sendMessage("§cFailed to migrate your ⚡ wallet. Please contact an admin.");
                            Bukkit.getLogger().warning("Migration failed for player " + player.getName() + ": " + e.getMessage());
                        }
                    } else {
                        // No user in old or new system, create a new wallet in V1
                        player.sendMessage("§eYour ⚡ wallet is being created.");
                        boolean created = lnbits.createWalletV1(player);
                        if (created) {
                            player.sendMessage("§aYour ⚡ wallet has been created!");
                            // Attempt to deposit starting balance
                            boolean deposited = lnbits.deposit(player.getUniqueId(), ConfigHandler.getStartingBalance());
                            if (!deposited) {
                                player.sendMessage("§cWarning: Could not deposit starting balance. Wallet may need funding.");
                            }
                        } else {
                            player.sendMessage("§cFailed to create your ⚡ wallet. Please contact an admin.");
                        }
                    }
                }

                // Clear any cached keys on join
                if (SCEconomy.playerAdminKey.containsKey(player.getUniqueId())) {
                    SCEconomy.playerAdminKey.remove(player.getUniqueId());
                }
                if (SCEconomy.playerInKey.containsKey(player.getUniqueId())) {
                    SCEconomy.playerInKey.remove(player.getUniqueId());
                }

                // Create LNURL-Pay link if enabled
                if (ConfigHandler.getLNAddress()) {
                    lnbits.createlnurlp(player.getUniqueId(), "SCLNAddress", 10, 5000000, "SCLNAddress", player.getName());
                }
            }
        }.runTaskAsynchronously(SCEconomy.getInstance());
    }
}