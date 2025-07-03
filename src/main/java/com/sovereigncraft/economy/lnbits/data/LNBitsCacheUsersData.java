package com.sovereigncraft.economy.lnbits.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sovereigncraft.economy.SCE;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class LNBitsCacheUsersData {
    private static final Gson gson = new Gson();
    private static final File file = new File(
        new File(SCE.getInstance().getDataFolder(), "cache"), "users.json"
    );

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

    public static void load() {
        if (!file.exists()) return;
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
            Map<String, Map<String, Object>> loaded = gson.fromJson(reader, type);
            if (loaded != null) userCache.putAll(loaded);
        } catch (IOException e) {
            SCE.getInstance().getLogger().warning("Failed to load LNBits user cache: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            File parent = file.getParentFile();
            if (!parent.exists()) parent.mkdirs();

            try (Writer writer = new FileWriter(file)) {
                gson.toJson(userCache, writer);
            }
        } catch (IOException e) {
            SCE.getInstance().getLogger().warning("Failed to save LNBits user cache: " + e.getMessage());
        }
    }
}
