package com.sovereigncraft.economy.lnbits.logic;

import com.sovereigncraft.economy.ConfigHandler;
import com.sovereigncraft.economy.lnbits.LNBitsUtils;
import com.sovereigncraft.economy.lnbits.data.LNBitsCacheUsersData;
import com.sovereigncraft.economy.lnbits.data.LNBitsUserCache;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Logic class responsible for fetching and caching LNBits user + wallet data.
 */
public class LNBitsCacheUsersLogic {

    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();


    public LNBitsUserCache get(UUID uuid) {
        String username = LNBitsUtils.getHashedUsername(uuid.toString());
        Map<String, Object> raw = LNBitsCacheUsersData.getCachedUser(username);
        if (raw == null) return null;
        return new LNBitsUserCache(raw);
    }


    /**
     * Get cached user or fetch from LNBits and cache result.
     *
     * @param uuid Minecraft player's UUID
     * @return Map of LNBits user object, including a "wallets" key
     */
    public Map<String, Object> getOrFetchAndCacheUserWithWallets(UUID uuid) {
        String username = LNBitsUtils.getHashedUsername(uuid.toString());
        Map<String, Object> cached = LNBitsCacheUsersData.getCachedUser(username);

        if (cached != null && cached.containsKey("wallets")) {
            return cached;
        }

        try {
            // === Fetch User ===
            String userUrl = "https://" + ConfigHandler.getHost() + "/users/api/v1/user/" + username;
            HttpRequest userReq = HttpRequest.newBuilder()
                    .uri(URI.create(userUrl))
                    .header("Authorization", "Bearer " + ConfigHandler.getBearerToken("users"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> userRes = client.send(userReq, HttpResponse.BodyHandlers.ofString());
            if (userRes.statusCode() != 200)
                throw new RuntimeException("Failed to fetch user. Status: " + userRes.statusCode());

            Map<String, Object> user = gson.fromJson(userRes.body(), new TypeToken<Map<String, Object>>() {}.getType());

            // === Fetch Wallets ===
            String userId = (String) user.get("id");
            String walletsUrl = "https://" + ConfigHandler.getHost() + "/users/api/v1/user/" + userId + "/wallet";
            HttpRequest walletReq = HttpRequest.newBuilder()
                    .uri(URI.create(walletsUrl))
                    .header("Authorization", "Bearer " + ConfigHandler.getBearerToken("users"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> walletRes = client.send(walletReq, HttpResponse.BodyHandlers.ofString());
            if (walletRes.statusCode() != 200)
                throw new RuntimeException("Failed to fetch wallets. Status: " + walletRes.statusCode());

            List<Map<String, Object>> wallets = gson.fromJson(walletRes.body(), new TypeToken<List<Map<String, Object>>>() {}.getType());
            user.put("wallets", wallets);

            // Cache and return
            LNBitsCacheUsersData.putCachedUser(username, user);
            return user;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching user and wallets for UUID: " + uuid, e);
        }
    }
}
