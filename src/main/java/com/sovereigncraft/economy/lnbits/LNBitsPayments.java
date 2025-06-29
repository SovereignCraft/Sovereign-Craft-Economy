package com.sovereigncraft.economy.lnbits;

import com.sovereigncraft.economy.ConfigHandler;

import com.google.gson.Gson;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * Helper class for interacting with LNBits payment endpoints. This includes
 * creating invoices, paying them and checking their status.
 */
public class LNBitsPayments {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static final String PAYMENTS_ENDPOINT = "https://" + ConfigHandler.getHost() + "/api/v1/payments";

    /**
     * Creates a BOLT11 invoice for the given amount in satoshis. Requires the
     * invoice key ({@code inkey}) from the user's wallet.
     *
     * @param inkey  wallet invoice key
     * @param amount amount in satoshis
     * @return BOLT11 invoice string
     * @throws RuntimeException if the request fails or is interrupted
     */
    public String createInvoice(String inkey, double amount) {
        return createInvoice(inkey, amount, "Sovereign Craft");
    }

    /**
     * Creates a BOLT11 invoice with a custom memo.
     *
     * @param inkey  wallet invoice key
     * @param amount amount in satoshis
     * @param memo   memo to attach to the invoice
     * @return BOLT11 invoice string
     * @throws RuntimeException if the request fails or is interrupted
     */
    public String createInvoice(String inkey, double amount, String memo) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("out", false);
        payload.put("amount", Math.round(amount));  // LNBits expects amount in sats
        payload.put("memo", memo);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PAYMENTS_ENDPOINT))
                .headers("Authorization", "Bearer " + inkey, "Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            debugLog("[createInvoice] " + response.statusCode() + " - " + response.body());

            if (response.statusCode() != 201 && response.statusCode() != 200) {
                throw new RuntimeException("Failed to create invoice: " + response.body());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = gson.fromJson(response.body(), Map.class);
            debugLog("[createInvoice] payment_hash=" + result.get("payment_hash"));
            return (String) result.get("payment_request");

        } catch (IOException e) {
            throw new RuntimeException("Error creating invoice", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error creating invoice", e);
        }
    }

    /**
     * Pays a BOLT11 invoice using the admin key of the sender's wallet.
     *
     * @param adminkey admin key of the paying wallet
     * @param bolt11   encoded invoice to pay
     * @return {@code true} if the payment request was accepted
     * @throws RuntimeException if the request fails or is interrupted
     */
    public boolean payInvoice(String adminkey, String bolt11) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("out", true);
        payload.put("bolt11", bolt11);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PAYMENTS_ENDPOINT))
                .headers("Authorization", "Bearer " + adminkey, "Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            debugLog("[payInvoice] " + response.statusCode() + " - " + response.body());

            return response.statusCode() == 200;

        } catch (IOException e) {
            throw new RuntimeException("Error paying invoice", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error paying invoice", e);
        }
    }

    /**
     * Checks the status of an invoice using the payment hash.
     *
     * @param adminkey    admin key of the wallet used to query
     * @param paymentHash payment hash returned when the invoice was created
     * @return {@code true} if the invoice has been paid
     * @throws RuntimeException if the request fails or is interrupted
     */
    public boolean getInvoiceStatus(String adminkey, String paymentHash) {
        String url = PAYMENTS_ENDPOINT + "/" + paymentHash;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers("Authorization", "Bearer " + adminkey)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            debugLog("[getInvoiceStatus] " + response.statusCode() + " - " + response.body());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch invoice status: " + response.body());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = gson.fromJson(response.body(), Map.class);
            Object paidObj = result.get("paid");

            return paidObj != null && Boolean.TRUE.equals(paidObj);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Error checking invoice status", e);
        }
    }

    /**
     * Print a debug message to operators and the server console when debug
     * mode is active.
     */
    private static void debugLog(String msg) {
        if (ConfigHandler.getDebug()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    player.sendMessage("§7[§bLNBitsPayments§7] §f" + msg);
                }
            }
            Bukkit.getLogger().info("[LNBitsPayments] " + msg);
        }
    }
}
