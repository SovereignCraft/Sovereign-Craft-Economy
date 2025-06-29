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

public class LNBitsUsers {

    private static final String USERS_ENDPOINT = "https://" + ConfigHandler.getHost() + "/users/api/v1/user";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    /**
     * Create a new LNBits user using a SHA-256 hash of the UUID, prefixed with "mc_".
     * @param uuid Minecraft UUID of the player
     * @return true if user creation was successful
     */
    public boolean createUser(UUID uuid) {
        String username = LNBitsUtils.getHashedUsername(uuid.toString());
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USERS_ENDPOINT))
                .headers("Content-Type", "application/json", "Authorization", "Bearer " + ConfigHandler.getBearerToken("Users"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            debugLog("[createUser] Response: " + response.statusCode() + " - " + response.body());
            return response.statusCode() == 201 || response.statusCode() == 200 || response.statusCode() == 409;
        } catch (IOException e) {
            throw new RuntimeException("Error creating user: " + uuid, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error creating user: " + uuid, e);
        }
    }

    /**
     * Retrieve a user from LNBits by Minecraft UUID (converted to hashed username).
     * @param uuid Minecraft UUID
     * @return Map containing user details
     */
    public Map<String, Object> getUser(UUID uuid) {
        String username = LNBitsUtils.getHashedUsername(uuid.toString());
        String url = USERS_ENDPOINT + "?username=" + username;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers("Content-Type", "application/json", "Authorization", "Bearer " + ConfigHandler.getBearerToken("Users"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            debugLog("[getUser] Response: " + response.statusCode() + " - " + response.body());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch user for UUID: " + uuid);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> jsonResponse = gson.fromJson(response.body(), Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> users = (List<Map<String, Object>>) jsonResponse.get("data");

            if (users == null || users.isEmpty()) {
                throw new NullPointerException("User not found: " + uuid);
            }

            return users.get(0);

        } catch (IOException e) {
            throw new RuntimeException("Error fetching user: " + uuid, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error fetching user: " + uuid, e);
        }
    }

    /**
     * Checks if a user exists for the given UUID.
     */
    public boolean userExists(UUID uuid) {
        try {
            getUser(uuid);
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Optional debug log to console and OP players
     */
    private void debugLog(String message) {
        if (ConfigHandler.getDebug()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    player.sendMessage("§7[§bLNBitsUsers§7] §f" + message);
                }
            }
            Bukkit.getLogger().info("[LNBitsUsers] " + message);
        }
    }
}
