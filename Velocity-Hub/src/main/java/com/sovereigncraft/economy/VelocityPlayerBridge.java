package com.sovereigncraft.economy;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.UUID;

public class VelocityPlayerBridge implements PlayerBridge {

    private final ProxyServer server;

    public VelocityPlayerBridge(ProxyServer server) {
        this.server = server;
    }

    @Override
    public String getPlayerName(UUID uuid) {
        return server.getPlayer(uuid).map(Player::getUsername).orElse(null);
    }

    @Override
    public boolean isPlayerOnline(UUID uuid) {
        return server.getPlayer(uuid).isPresent();
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        Optional<Player> player = server.getPlayer(uuid);
        player.ifPresent(p -> p.sendMessage(Component.text(message)));
    }
}