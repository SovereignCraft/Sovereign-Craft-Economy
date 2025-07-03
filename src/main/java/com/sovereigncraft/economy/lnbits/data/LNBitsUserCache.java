package com.sovereigncraft.economy.lnbits.data;

import java.util.Map;

public class LNBitsUserCache {
    private final Map<String, Object> data;

    public LNBitsUserCache(Map<String, Object> data) {
        this.data = data;
    }

    public String adminkey() {
        return (String) data.getOrDefault("admin_key", null);
    }

    public String inkey() {
        return (String) data.getOrDefault("invoice_key", null);
    }

    public Map<String, Object> getRaw() {
        return data;
    }
}
