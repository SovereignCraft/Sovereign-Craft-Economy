package com.sovereigncraft.economy;

import java.util.UUID;

public interface PlayerBridge {
    String getPlayerName(UUID uuid);
    boolean isPlayerOnline(UUID uuid);
    void sendMessage(UUID uuid, String message);
}
