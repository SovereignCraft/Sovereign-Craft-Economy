package com.sovereigncraft.economy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "velocity-hub",
        name = "Velocity-Hub",
        version = "1.0-SNAPSHOT",
        description = "The hub for Sovereign Craft Economy",
        authors = {"YourName"}
)
public class VelocityHub {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private ConfigHandler configHandler;

    public static final MinecraftChannelIdentifier INCOMING_CHANNEL = MinecraftChannelIdentifier.create("sceconomy", "incoming");
    public static final MinecraftChannelIdentifier OUTGOING_CHANNEL = MinecraftChannelIdentifier.create("sceconomy", "outgoing");

    @Inject
    public VelocityHub(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.configHandler = new ConfigHandler(dataDirectory);

        server.getChannelRegistrar().register(INCOMING_CHANNEL, OUTGOING_CHANNEL);
        server.getEventManager().register(this, new MessageListener(this));

        logger.info("Velocity-Hub has been enabled!");
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }
}