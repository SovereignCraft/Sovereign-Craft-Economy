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
 * Wrapper around the LNBits user management API. Methods in this class
 * allow creation and retrieval of LNBits users based on Minecraft UUIDs.
 */
public class LNBitsUsers {

    private static final String CREATE_USER_ENDPOINT = "https://" + ConfigHandler.getHost() + "/users/api/v1/user";

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    /**
     * Create a new LNBits user using a SHA-256 hash of the UUID prefixed with {@code mc_}.
     *
     * @param uuid Minecraft UUID of the player
     * @return {@code true} if user creation was successful
     */
    public boolean createUser(UUID uuid) {
        String username = LNBitsUtils.getHashedUsername(uuid.toString());
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CREATE_USER_ENDPOINT))
                .headers(
                        "Content-Type", "application/json",
                        "Authorization", "Bearer " + ConfigHandler.getBearerToken("default"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            debugLog("[createUser] Response: " + response.statusCode() + " - " + response.body());
            return response.statusCode() == 201 || response.statusCode() == 200 || response.statusCode() == 409;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error creating user: " + uuid, e);
        }
    }

    /**
     * Outputs a debug message to the console and any online operators when debug mode is enabled.
     *
     * @param message The message to log if debug mode is enabled.
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
