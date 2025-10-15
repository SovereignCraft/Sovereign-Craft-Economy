package com.sovereigncraft.economy;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class json {
    public static Map<String, Object> JSON2Map(String jsonString) {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> result = gson.fromJson(jsonString, type);
            if (result == null) {
                Bukkit.getLogger().warning("JSON2Map returned null for input: " + jsonString);
            }
            return result;
        } catch (JsonSyntaxException e) {
            Bukkit.getLogger().warning("JSON2Map failed to parse: " + jsonString);
            Bukkit.getLogger().warning("Exception: " + e.getMessage());
            throw e; // Rethrow to allow calling methods to handle the error
        }
    }

    public static List<Object> JSON2List(String jsonString) {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Object>>() {}.getType();
            List<Object> result = gson.fromJson(jsonString, type);
            if (result == null) {
                Bukkit.getLogger().warning("JSON2List returned null for input: " + jsonString);
            }
            return result;
        } catch (JsonSyntaxException e) {
            Bukkit.getLogger().warning("JSON2List failed to parse: " + jsonString);
            Bukkit.getLogger().warning("Exception: " + e.getMessage());
            throw e;
        }
    }
}