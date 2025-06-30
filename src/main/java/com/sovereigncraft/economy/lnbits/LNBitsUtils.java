package com.sovereigncraft.economy.lnbits;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sovereigncraft.economy.LogHandler;

/**
 * Collection of helper utilities used throughout the LNBits integration.
 */
public class LNBitsUtils {

    /**
     * Generates a SHA-256 hash of the given input and trims the result to
     * 16 characters. This helper is used to convert Minecraft UUIDs to valid
     * LNBits usernames.
     *
     * @param input value to hash
     * @return the first 16 hexadecimal characters of the SHA-256 hash
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            // Truncate to 16 characters to meet LNBits username constraints
            return hexString.toString().substring(0, 16);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported on this platform", e);
        }
    }

    /**
     * Builds a username string from a UUID using the {@link #sha256(String)}
     * helper. The resulting username is in the form {@code mc_<hash>}.
     *
     * @param uuid UUID to convert
     * @return hashed username string
     */
    public static String getHashedUsername(String uuid) {
        return "mc_" + sha256(uuid);
    }

    /**
     * Converts a {@link UUID} to a valid LNBits username.
     *
     * @param uuid UUID instance
     * @return hashed username string
     */
    public static String uuidToUsername(UUID uuid) {
        return getHashedUsername(uuid.toString());
    }

    /**
     * Format a number of satoshis with comma separators and no decimals.
     *
     * @param sats value in satoshis
     * @return formatted satoshi amount
     */
    public static String formatSats(double sats) {
        DecimalFormat df = new DecimalFormat("###,###,##0");
        df.setGroupingSize(3);
        return df.format(Math.floor(sats));
    }

    /**
     * Format a fiat currency amount with comma separators and two decimals.
     *
     * @param amount numeric amount
     * @return formatted amount in standard currency notation
     */
    public static String formatFiat(double amount) {
        DecimalFormat df = new DecimalFormat("###,###,##0.00");
        df.setGroupingSize(3);
        return df.format(amount);
    }

    /**
     * Ensures the command sender is a player.
     *
     * @param sender CommandSender to check.
     * @return the Player if valid, otherwise null.
     */
    public static Player requirePlayer(CommandSender sender, String commandName) {
        if (sender instanceof Player player) {
            return player;
        } else {
            sender.sendMessage("Only players can use this command.");
            LogHandler.logAction(commandName + "_rejected", Map.of(
                "sender", sender.getName(),
                "reason", "non-player"
            ));
            return null;
        }
    }
}
