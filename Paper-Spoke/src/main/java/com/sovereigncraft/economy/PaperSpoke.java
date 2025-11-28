package com.sovereigncraft.economy;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.io.ByteArrayDataInput;
import com.sovereigncraft.economy.commands.BalanceCommand;
import com.sovereigncraft.economy.commands.LNCommand;
import com.sovereigncraft.economy.commands.LN_autocompletation;
import com.sovereigncraft.economy.commands.PayCommand;
import com.sovereigncraft.economy.eco.VaultImpl;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class PaperSpoke extends JavaPlugin implements PluginMessageListener {

    private ConfigHandler configHandler;
    private Economy economy = null;
    public static final String INCOMING_CHANNEL = "sceconomy:incoming";
    public static final String OUTGOING_CHANNEL = "sceconomy:outgoing";
    
    private final ConcurrentHashMap<UUID, CompletableFuture<Double>> balanceFutures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, CompletableFuture<Boolean>> hasAccountFutures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, CompletableFuture<EconomyResponse>> transactionFutures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, CompletableFuture<String>> invoiceFutures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, CompletableFuture<String>> userIdFutures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, CompletableFuture<String>> adminKeyFutures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, CompletableFuture<Boolean>> sendFutures = new ConcurrentHashMap<>();
    private final CompletableFuture<List<String>> currenciesFuture = new CompletableFuture<>();
    public final Map<UUID, String> playerQRInterface = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.configHandler = new ConfigHandler(getConfig());

        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, OUTGOING_CHANNEL);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, INCOMING_CHANNEL, this);
        
        this.getCommand("balance").setExecutor(new BalanceCommand(this, economy));
        this.getCommand("pay").setExecutor(new PayCommand(this, economy));
        this.getCommand("ln").setExecutor(new LNCommand(this, economy));
        this.getCommand("ln").setTabCompleter(new LN_autocompletation(this));

        getLogger().info("Paper-Spoke has been enabled!");
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        economy = new VaultImpl(this);
        getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.Normal);
        return true;
    }

    public CompletableFuture<Double> getBalance(Player player) {
        CompletableFuture<Double> future = new CompletableFuture<>();
        balanceFutures.put(player.getUniqueId(), future);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("getBalance");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(configHandler.getFloatUUID().toString());
        
        if (player != null && player.isOnline()) {
            player.sendPluginMessage(this, OUTGOING_CHANNEL, out.toByteArray());
        } else {
            future.completeExceptionally(new RuntimeException("Player is not online to send plugin message"));
        }
        
        return future;
    }
    
    public CompletableFuture<Boolean> hasAccount(Player player) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        hasAccountFutures.put(player.getUniqueId(), future);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("hasAccount");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(configHandler.getFloatUUID().toString());

        if (player != null && player.isOnline()) {
            player.sendPluginMessage(this, OUTGOING_CHANNEL, out.toByteArray());
        } else {
            future.completeExceptionally(new RuntimeException("Player is not online to send plugin message"));
        }

        return future;
    }

    public void createPlayerAccount(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("createPlayerAccount");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(configHandler.getFloatUUID().toString());

        if (player != null && player.isOnline()) {
            player.sendPluginMessage(this, OUTGOING_CHANNEL, out.toByteArray());
        }
    }

    public CompletableFuture<EconomyResponse> depositPlayer(Player player, double amount) {
        CompletableFuture<EconomyResponse> future = new CompletableFuture<>();
        transactionFutures.put(player.getUniqueId(), future);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("depositPlayer");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(configHandler.getFloatUUID().toString());
        out.writeDouble(amount);

        if (player != null && player.isOnline()) {
            player.sendPluginMessage(this, OUTGOING_CHANNEL, out.toByteArray());
        } else {
            future.completeExceptionally(new RuntimeException("Player is not online to send plugin message"));
        }

        return future;
    }

    public CompletableFuture<EconomyResponse> withdrawPlayer(Player player, double amount) {
        CompletableFuture<EconomyResponse> future = new CompletableFuture<>();
        transactionFutures.put(player.getUniqueId(), future);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("withdrawPlayer");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(configHandler.getFloatUUID().toString());
        out.writeDouble(amount);

        if (player != null && player.isOnline()) {
            player.sendPluginMessage(this, OUTGOING_CHANNEL, out.toByteArray());
        } else {
            future.completeExceptionally(new RuntimeException("Player is not online to send plugin message"));
        }

        return future;
    }
    
    public CompletableFuture<String> createInvoice(Player player, double amount) {
        CompletableFuture<String> future = new CompletableFuture<>();
        invoiceFutures.put(player.getUniqueId(), future);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("createInvoice");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(configHandler.getFloatUUID().toString());
        out.writeDouble(amount);

        if (player != null && player.isOnline()) {
            player.sendPluginMessage(this, OUTGOING_CHANNEL, out.toByteArray());
        } else {
            future.completeExceptionally(new RuntimeException("Player is not online to send plugin message"));
        }

        return future;
    }

    public CompletableFuture<String> createLNURLWithdraw(Player player, double amount) {
        CompletableFuture<String> future = new CompletableFuture<>();
        invoiceFutures.put(player.getUniqueId(), future);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("createLNURLWithdraw");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(configHandler.getFloatUUID().toString());
        out.writeDouble(amount);

        if (player != null && player.isOnline()) {
            player.sendPluginMessage(this, OUTGOING_CHANNEL, out.toByteArray());
        } else {
            future.completeExceptionally(new RuntimeException("Player is not online to send plugin message"));
        }

        return future;
    }
    
    public CompletableFuture<String> getUserId(Player player) {
        CompletableFuture<String> future = new CompletableFuture<>();
        userIdFutures.put(player.getUniqueId(), future);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("getUserId");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(configHandler.getFloatUUID().toString());

        if (player != null && player.isOnline()) {
            player.sendPluginMessage(this, OUTGOING_CHANNEL, out.toByteArray());
        } else {
            future.completeExceptionally(new RuntimeException("Player is not online to send plugin message"));
        }

        return future;
    }
    
    public CompletableFuture<String> getWalletAdminKey(Player player) {
        CompletableFuture<String> future = new CompletableFuture<>();
        adminKeyFutures.put(player.getUniqueId(), future);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("getWalletAdminKey");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(configHandler.getFloatUUID().toString());

        if (player != null && player.isOnline()) {
            player.sendPluginMessage(this, OUTGOING_CHANNEL, out.toByteArray());
        } else {
            future.completeExceptionally(new RuntimeException("Player is not online to send plugin message"));
        }

        return future;
    }
    
    public CompletableFuture<Boolean> sendLNAddress(Player player, String lnAddress, double amount) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        sendFutures.put(player.getUniqueId(), future);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("sendLNAddress");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(configHandler.getFloatUUID().toString());
        out.writeUTF(lnAddress);
        out.writeDouble(amount);

        if (player != null && player.isOnline()) {
            player.sendPluginMessage(this, OUTGOING_CHANNEL, out.toByteArray());
        } else {
            future.completeExceptionally(new RuntimeException("Player is not online to send plugin message"));
        }

        return future;
    }
    
    public CompletableFuture<List<String>> getCurrencies() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("getCurrencies");
        out.writeUTF(UUID.randomUUID().toString()); // Dummy UUID
        out.writeUTF(configHandler.getFloatUUID().toString());

        // Note: This requires at least one player to be online on this server.
        Player sender = getServer().getOnlinePlayers().stream().findAny().orElse(null);
        if (sender != null) {
            sender.sendPluginMessage(this, OUTGOING_CHANNEL, out.toByteArray());
        } else {
            currenciesFuture.completeExceptionally(new RuntimeException("No players online to send plugin message"));
        }

        return currenciesFuture;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(INCOMING_CHANNEL)) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();

        if (subChannel.equals("balance")) {
            UUID playerUuid = UUID.fromString(in.readUTF());
            double balance = in.readDouble();
            
            CompletableFuture<Double> future = balanceFutures.remove(playerUuid);
            if (future != null) {
                future.complete(balance);
            }
        } else if (subChannel.equals("hasAccount")) {
            UUID playerUuid = UUID.fromString(in.readUTF());
            boolean hasAccount = in.readBoolean();

            CompletableFuture<Boolean> future = hasAccountFutures.remove(playerUuid);
            if (future != null) {
                future.complete(hasAccount);
            }
        } else if (subChannel.equals("transactionResponse")) {
            UUID playerUuid = UUID.fromString(in.readUTF());
            boolean success = in.readBoolean();
            double balance = in.readDouble();
            double amount = in.readDouble();
            String errorMessage = in.readUTF();
            
            EconomyResponse.ResponseType type = success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE;
            EconomyResponse response = new EconomyResponse(amount, balance, type, errorMessage);

            CompletableFuture<EconomyResponse> future = transactionFutures.remove(playerUuid);
            if (future != null) {
                future.complete(response);
            }
        } else if (subChannel.equals("invoice")) {
            UUID playerUuid = UUID.fromString(in.readUTF());
            String invoice = in.readUTF();

            CompletableFuture<String> future = invoiceFutures.remove(playerUuid);
            if (future != null) {
                future.complete(invoice);
            }
        } else if (subChannel.equals("userId")) {
            UUID playerUuid = UUID.fromString(in.readUTF());
            String userId = in.readUTF();

            CompletableFuture<String> future = userIdFutures.remove(playerUuid);
            if (future != null) {
                future.complete(userId);
            }
        } else if (subChannel.equals("walletAdminKey")) {
            UUID playerUuid = UUID.fromString(in.readUTF());
            String adminKey = in.readUTF();

            CompletableFuture<String> future = adminKeyFutures.remove(playerUuid);
            if (future != null) {
                future.complete(adminKey);
            }
        } else if (subChannel.equals("sendResponse")) {
            UUID playerUuid = UUID.fromString(in.readUTF());
            boolean success = in.readBoolean();

            CompletableFuture<Boolean> future = sendFutures.remove(playerUuid);
            if (future != null) {
                future.complete(success);
            }
        } else if (subChannel.equals("currencies")) {
            int size = in.readInt();
            List<String> currencies = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                currencies.add(in.readUTF());
            }
            currenciesFuture.complete(currencies);
        }
    }
    
    public ConfigHandler getConfigHandler() {
        return configHandler;
    }
    
    public Economy getEconomy() {
        return economy;
    }
}