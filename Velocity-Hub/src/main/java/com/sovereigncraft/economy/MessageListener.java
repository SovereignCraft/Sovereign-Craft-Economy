package com.sovereigncraft.economy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class MessageListener {

    private final VelocityHub plugin;
    private final MinecraftChannelIdentifier outgoingChannel;

    public MessageListener(VelocityHub plugin) {
        this.plugin = plugin;
        this.outgoingChannel = VelocityHub.OUTGOING_CHANNEL;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(VelocityHub.INCOMING_CHANNEL)) {
            return;
        }

        // The source of the message is the server it came from
        ServerConnection serverConnection = (ServerConnection) event.getSource();

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String subChannel = in.readUTF();

        // The UUID of the player who is the subject of the economy operation
        UUID playerUuid = UUID.fromString(in.readUTF());

        // The UUID of the float account for the spoke
        UUID floatUuid = UUID.fromString(in.readUTF());
        
        plugin.getLogger().info("Received message on subchannel " + subChannel + " for player " + playerUuid + " from spoke " + floatUuid);

        try {
            // Instantiate LNBits with the floatUUID from the spoke
            LNBits lnbits = new LNBits(
                    plugin.getConfigHandler().getHost(),
                    Integer.parseInt(plugin.getConfigHandler().getPort()),
                    plugin.getConfigHandler().getAdminKey(),
                    plugin.getConfigHandler().getAccessToken(),
                    plugin.getConfigHandler().getBedrockPrefix(),
                    plugin.getConfigHandler().getLNBitsBedrockSuffix(),
                    0, // Starting balance is handled by the spoke
                    floatUuid,
                    new VelocityPlayerBridge(plugin.getServer()),
                    java.util.logging.Logger.getLogger("LNBits")
            );

            // Handle the specific request based on the subchannel
            if ("getBalance".equals(subChannel)) {
                double balance = lnbits.getBalance(playerUuid);

                // Send the balance back to the spoke
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("balance");
                out.writeUTF(playerUuid.toString());
                out.writeDouble(balance);

                serverConnection.sendPluginMessage(outgoingChannel, out.toByteArray());
            } else if ("hasAccount".equals(subChannel)) {
                boolean hasAccount = lnbits.hasAccount(playerUuid);

                // Send the result back to the spoke
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("hasAccount");
                out.writeUTF(playerUuid.toString());
                out.writeBoolean(hasAccount);

                serverConnection.sendPluginMessage(outgoingChannel, out.toByteArray());
            } else if ("createPlayerAccount".equals(subChannel)) {
                lnbits.createWalletV1(playerUuid);
            } else if ("depositPlayer".equals(subChannel)) {
                double amount = in.readDouble();
                boolean success = lnbits.deposit(playerUuid, amount);
                double balance = lnbits.getBalance(playerUuid);

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("transactionResponse");
                out.writeUTF(playerUuid.toString());
                out.writeBoolean(success);
                out.writeDouble(balance);
                out.writeDouble(amount);
                out.writeUTF(success ? "" : "Deposit failed.");

                serverConnection.sendPluginMessage(outgoingChannel, out.toByteArray());
            } else if ("withdrawPlayer".equals(subChannel)) {
                double amount = in.readDouble();
                boolean success = lnbits.withdraw(playerUuid, amount);
                double balance = lnbits.getBalance(playerUuid);

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("transactionResponse");
                out.writeUTF(playerUuid.toString());
                out.writeBoolean(success);
                out.writeDouble(balance);
                out.writeDouble(amount);
                out.writeUTF(success ? "" : "Withdrawal failed.");

                serverConnection.sendPluginMessage(outgoingChannel, out.toByteArray());
            } else if ("createInvoice".equals(subChannel)) {
                double amount = in.readDouble();
                String invoice = lnbits.createInvoice(playerUuid, amount);

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("invoice");
                out.writeUTF(playerUuid.toString());
                out.writeUTF(invoice);

                serverConnection.sendPluginMessage(outgoingChannel, out.toByteArray());
            } else if ("createLNURLWithdraw".equals(subChannel)) {
                double amount = in.readDouble();
                String lnurlw = lnbits.createlnurlw(playerUuid, amount);

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("lnurlw"); // Corrected subchannel
                out.writeUTF(playerUuid.toString());
                out.writeUTF(lnurlw);

                serverConnection.sendPluginMessage(outgoingChannel, out.toByteArray());
            } else if ("getUserId".equals(subChannel)) {
                Map user = lnbits.getUser(playerUuid);
                String userId = user != null ? (String) user.get("id") : "";

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("userId");
                out.writeUTF(playerUuid.toString());
                out.writeUTF(userId);

                serverConnection.sendPluginMessage(outgoingChannel, out.toByteArray());
            } else if ("getWalletAdminKey".equals(subChannel)) {
                String adminKey = lnbits.getWalletAdminKey(playerUuid);

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("walletAdminKey");
                out.writeUTF(playerUuid.toString());
                out.writeUTF(adminKey);

                serverConnection.sendPluginMessage(outgoingChannel, out.toByteArray());
            } else if ("sendLNAddress".equals(subChannel)) {
                String lnAddress = in.readUTF();
                double amount = in.readDouble();
                boolean success = lnbits.sendLNAddress(playerUuid, lnAddress, amount);

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("sendResponse");
                out.writeUTF(playerUuid.toString());
                out.writeBoolean(success);

                serverConnection.sendPluginMessage(outgoingChannel, out.toByteArray());
            } else if ("getCurrencies".equals(subChannel)) {
                List<String> currencies = lnbits.getCurrencies();

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("currencies");
                out.writeInt(currencies.size());
                for (String currency : currencies) {
                    out.writeUTF(currency);
                }

                serverConnection.sendPluginMessage(outgoingChannel, out.toByteArray());
            }
        } catch (Exception e) {
            plugin.getLogger().error("Error processing plugin message", e);
        }
    }
}