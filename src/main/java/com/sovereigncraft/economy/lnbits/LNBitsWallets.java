package com.sovereigncraft.economy.lnbits;

import com.sovereigncraft.economy.ConfigHandler;

import com.google.gson.Gson;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * Provides convenience methods for interacting with wallet related endpoints
 * of the LNBits API.
 */
public class LNBitsWallets {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static final String BASE_USER_ENDPOINT = "https://" + ConfigHandler.getHost() + "/users/api/v1/user";

    /**
     * Retrieve all wallets for the specified LNBits {@code userId}. The value
     * for {@code userId} is typically obtained from {@link LNBitsUsers#getUser}.
     *
     * @param userId the LNBits user identifier
     * @return list of wallets belonging to the user
     * @throws RuntimeException    if the request fails
     * @throws NullPointerException if no wallets are found
     */
    public List<Map<String, Object>> getWallets(String userId) {
        String url = BASE_USER_ENDPOINT + "/" + userId + "/wallet";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers("Authorization", "Bearer " + ConfigHandler.getBearerToken("Wallets"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            debugLog("[getWallets] " + response.statusCode() + " - " + response.body());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch wallets for user: " + userId);
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> wallets = gson.fromJson(response.body(), List.class);

            if (wallets == null || wallets.isEmpty()) {
                throw new NullPointerException("No wallets found for user: " + userId);
            }

            return wallets;

        } catch (IOException e) {
            throw new RuntimeException("Error fetching LNBits wallets for userId: " + userId, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error fetching LNBits wallets for userId: " + userId, e);
        }
    }

    /**
     * Convenience method to fetch the first wallet for a user.
     *
     * @param userId the LNBits user identifier
     * @return the first wallet returned or {@code null} if none exist
     * @throws RuntimeException if wallet retrieval fails
     */
    public Map<String, Object> getWallet(String userId) {
        debugLog("[getWallet] Fetching first wallet for userId: " + userId);
        List<Map<String, Object>> wallets = getWallets(userId);

        if (wallets.isEmpty()) {
            debugLog("[getWallet] No wallets found for userId: " + userId);
            return null;
        }

        debugLog("[getWallet] Returning first wallet (name=" + wallets.get(0).get("name") + ")");
        return wallets.get(0);
    }

    /**
     * Shortcut to fetch the first wallet for a player UUID using
     * {@link LNBitsUsers}.
     *
     * @param uuid  Minecraft UUID of the player
     * @param users instance of {@link LNBitsUsers} to query
     * @return the player's first wallet or {@code null} if none exist
     * @throws RuntimeException if wallet retrieval fails
     */
    public Map<String, Object> getWalletByUUID(UUID uuid, LNBitsUsers users) {
        debugLog("[getWalletByUUID] Resolving user for UUID: " + uuid);
        Map<String, Object> user = users.getUser(uuid);
        String userId = (String) user.get("id");

        debugLog("[getWalletByUUID] Resolved userId: " + userId + " for UUID: " + uuid);
        return getWallet(userId);
    }

    /**
     * Print a debug message to operators and the console when debug mode is
     * enabled.
     */
    private static void debugLog(String msg) {
        if (ConfigHandler.getDebug()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    player.sendMessage("§7[§bLNBitsWallets§7] §f" + msg);
                }
            }
            Bukkit.getLogger().info("[LNBitsWallets] " + msg);
        }
    }
}
