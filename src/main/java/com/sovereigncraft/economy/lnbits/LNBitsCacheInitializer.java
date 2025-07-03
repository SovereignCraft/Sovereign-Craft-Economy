package com.sovereigncraft.economy.lnbits;

import com.sovereigncraft.economy.SCE;
import com.sovereigncraft.economy.lnbits.logic.LNBitsCacheUsersLogic;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class LNBitsCacheInitializer {

    public static void initializeAsync(JavaPlugin plugin) {
        Logger logger = plugin.getLogger();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            logger.info("[LNBitsCacheInitializer] Async cache initializer running...");

            List<UUID> knownUUIDs = Arrays.stream(Bukkit.getOfflinePlayers())
                              .map(player -> player.getUniqueId())
                              .toList();

            LNBitsCacheUsersLogic logic = SCE.getClient().cacheUsers();

            for (UUID uuid : knownUUIDs) {
                try {
                    logic.getOrFetchAndCacheUserWithWallets(uuid);
                    logger.info("[LNBitsCacheInitializer] Cached data for UUID: " + uuid);
                } catch (Exception e) {
                    logger.warning("[LNBitsCacheInitializer] Failed to cache user for UUID " + uuid + ": " + e.getMessage());
                }
            }

            logger.info("[LNBitsCacheInitializer] Async cache initialization complete.");
        });
    }
}
