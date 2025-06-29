package com.sovereigncraft.economy.lnbits;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class LNBitsUtils {

    /**
     * Generates a SHA-256 hash and trims to 16 characters.
     * Used to safely convert Minecraft UUIDs to valid LNBits usernames.
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
     * Builds a username string from a UUID using a prefix and SHA-256 hash.
     * The result will be in the format: mc_<hash>
     */
    public static String getHashedUsername(String uuid) {
        return "mc_" + sha256(uuid);
    }
}
