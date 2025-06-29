package com.sovereigncraft.economy.lnbits;

import com.google.gson.Gson;
import com.sovereigncraft.economy.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class LNBitsWallets {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static final String BASE_USER_ENDPOINT = "https://" + ConfigHandler.getHost() + "/users/api/v1/user";

    /**
     * Retrieve all wallets for the specified LNBits userId.
     * This userId is returned by LNBitsUsers.getUser().
     */
    public List<Map<String, Object>> getWallets(String userId) {
        String url = BASE_USER_ENDPOINT + "/" + userId + "/wallet";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers("Authorization", "Bearer " + ConfigHandler.getBearerToken("Users"))
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
     */
    public Map<String, Object> getWallet(String userId) {
        List<Map<String, Object>> wallets = getWallets(userId);
        return wallets.isEmpty() ? null : wallets.get(0);
    }

    /**
     * Shortcut to fetch the first wallet for a player UUID using LNBitsUsers.
     */
    public Map<String, Object> getWalletByUUID(UUID uuid, LNBitsUsers users) {
        Map<String, Object> user = users.getUser(uuid);
        String userId = (String) user.get("id");
        return getWallet(userId);
    }

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
