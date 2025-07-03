package com.sovereigncraft.economy.lnbits.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the raw user cache loaded from LNBits and/or disk.
 * Keys are hashed Minecraft UUID usernames → values are LNBits user JSON objects.
 */
public class LNBitsCacheUsersData {
    public static final Map<String, Map<String, Object>> userCache = new HashMap<>();

    public static Map<String, Object> getCachedUser(String username) {
        return userCache.get(username);
    }

    public static void putCachedUser(String username, Map<String, Object> userData) {
        userCache.put(username, userData);
    }

    public static boolean isCached(String username) {
        return userCache.containsKey(username);
    }

    public static Map<String, Map<String, Object>> getAll() {
        return userCache;
    }
}
