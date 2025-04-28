package com.sovereigncraft.economy;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LNBits100 {
    
    private static final String USERS_ENDPOINT = "https://" + ConfigHandler.getHost() + "/users/api/v1/user";
    private static HttpClient client = HttpClient.newHttpClient();
    private static Gson gson = new Gson();
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 16);  // Limit to 16 chars (for LNBits username constraint)
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found!", e);
        }
    }

    // ===== Create User with Wallet =====
    public static boolean createUser(UUID uuid) {
        Map<String, Object> payload = new HashMap<>();
        String username = "mc_" + sha256(uuid.toString());
        payload.put("username", username);  // Correct: set 'username' not 'id'
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USERS_ENDPOINT))
                .headers("Content-Type", "application/json", "Authorization", "Bearer " + ConfigHandler.getBearerToken("Users"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();
    
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            debugLog("[createUser] Response: " + response.statusCode() + " - " + response.body());

            return response.statusCode() == 201 || response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error creating user: " + uuid, e);
        }
    }
    
    // ===== Get User by UUID =====
    public static Map<String, Object> getUser(UUID uuid) {
        String url = USERS_ENDPOINT + "?username=" + uuid.toString();
    
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
    
            return users.get(0);  // Return the first (and only) user
    
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error fetching user: " + uuid, e);
        }
    }

    // ===== Get Wallets by userId =====
    public static List<Map<String, Object>> getWallets(String userId) throws IOException, InterruptedException {
        String url = USERS_ENDPOINT + "/" + userId + "/wallet";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers("Content-Type", "application/json","Authorization", "Bearer " + ConfigHandler.getBearerToken("Users"))
                .GET()
                .build();
        
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            debugLog("[getWallets] Response: " + response.statusCode() + " - " + response.body());
        
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch wallets for user: " + userId);
            }
        
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> wallets = gson.fromJson(response.body(), List.class);
        
            if (wallets == null || wallets.isEmpty()) {
                throw new NullPointerException("No wallets found for user: " + userId);
            }
    
            return wallets;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error fetching wallets: " + userId, e);
        }
    }

    // ===== Debug Logging =====
    private static void debugLog(String message) {
        if (ConfigHandler.getDebug()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    player.sendMessage("§7[§bDEBUG§7] §f" + message);
                }
            }
            Bukkit.getLogger().info("[LNBits100 - DEBUG] " + message);
        }
    }
}
