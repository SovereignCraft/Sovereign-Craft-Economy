package com.sovereigncraft.economy.lnbits;

import com.google.gson.Gson;
import com.sovereigncraft.economy.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * Caches all LNBits users and their associated wallets at startup.
 */
public class LNBitsCache {

    private static final Map<String, Map<String, Object>> userCache = new HashMap<>();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    /**
     * Fetches all users and their wallets from LNBits and caches them.
     * Should be run asynchronously.
     */
    public static void loadAllUsers() {
        Bukkit.getLogger().info("[LNBitsCache] Fetching all users from LNBits...");

        int offset = 0;
        int limit = 100;
        int totalUsers = 0;

        while (true) {
            String url = "https://" + ConfigHandler.getHost() + "/users/api/v1/user?limit=" + limit + "&offset=" + offset;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .headers(
                            "Authorization", "Bearer " + ConfigHandler.getBearerToken("users"),
                            "Content-Type", "application/json"
                    )
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    Bukkit.getLogger().warning("[LNBitsCache] Failed to fetch users (offset " + offset + "). HTTP " + response.statusCode());
                    break;
                }

                Map<String, Object> json = gson.fromJson(response.body(), Map.class);
                List<Map<String, Object>> users = (List<Map<String, Object>>) json.get("data");

                if (users == null || users.isEmpty()) {
                    break;
                }

                for (Map<String, Object> user : users) {
                    String username = (String) user.get("username");
                    String userId = (String) user.get("id");

                    // Fetch wallets for this user
                    HttpRequest walletReq = HttpRequest.newBuilder()
                            .uri(URI.create("https://" + ConfigHandler.getHost() + "/users/api/v1/user/" + userId + "/wallet"))
                            .headers(
                                    "Authorization", "Bearer " + ConfigHandler.getBearerToken("users"),
                                    "Content-Type", "application/json"
                            )
                            .GET()
                            .build();

                    HttpResponse<String> walletRes = client.send(walletReq, HttpResponse.BodyHandlers.ofString());

                    if (walletRes.statusCode() == 200) {
                        List<Map<String, Object>> wallets = gson.fromJson(walletRes.body(), List.class);
                        user.put("wallets", wallets);
                        userCache.put(username, user);
                        totalUsers++;
                    } else {
                        Bukkit.getLogger().warning("[LNBitsCache] Failed to fetch wallets for user '" + username + "' (HTTP " + walletRes.statusCode() + ")");
                    }
                }

                offset += limit;
            } catch (Exception e) {
                Bukkit.getLogger().severe("[LNBitsCache] Exception during user cache load: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }

        Bukkit.getLogger().info("[LNBitsCache] Cached " + totalUsers + " users.");
    }

    /**
     * Triggers cache load asynchronously. Call from plugin onEnable().
     * @param plugin Your JavaPlugin instance
     */
    public static void initializeAsync(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, LNBitsCache::loadAllUsers);
    }

    /**
     * Returns the full cached user map.
     */
    public static Map<String, Map<String, Object>> getAllCachedUsers() {
        return userCache;
    }

    /**
     * Returns a single cached user by hashed username.
     */
    public static Map<String, Object> getCachedUser(String username) {
        return userCache.get(username);
    }

    /**
     * Returns a cached user by Minecraft UUID (SHA-256 hashed username).
     */
    public static Map<String, Object> getCachedUserByUUID(UUID uuid) {
        String hashed = LNBitsUtils.getHashedUsername(uuid.toString());
        return getCachedUser(hashed);
    }

    /**
     * Updated getUser method that first attempts to use the cache, falling back to direct API request.
     */
    public static Map<String, Object> getUser(UUID uuid) {
        String username = LNBitsUtils.getHashedUsername(uuid.toString());

        // Try cache first
        Map<String, Object> cached = getCachedUser(username);
        if (cached != null) {
            return cached;
        }

        // Fallback: fetch directly from LNBits
        String url = "https://" + ConfigHandler.getHost() + "/users/api/v1/user/" + username;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers(
                        "Content-Type", "application/json",
                        "Authorization", "Bearer " + ConfigHandler.getBearerToken("users"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                throw new NullPointerException("User not found: " + uuid);
            }

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch user for UUID: " + uuid + ". Status: " + response.statusCode());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> user = gson.fromJson(response.body(), Map.class);
            return user;

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error fetching user: " + uuid, e);
        }
    }
}
