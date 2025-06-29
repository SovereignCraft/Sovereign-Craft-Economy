package com.sovereigncraft.economy.lnbits;

/**
 * Root client that exposes domain specific LNBits API wrappers.
 * <p>
 * This class acts as the entry point for interacting with users, wallets,
 * payments, caching and utility functions provided by the LNBits integration.
 */
public class LNBitsClient {

    // Root client references for domain-specific operations
    private final LNBitsUsers users;
    private final LNBitsWallets wallets;
    private final LNBitsPayments payments;
    private final LNBitsUtils utils;
    private final LNBitsCacheUsers cache;

    /**
     * Constructs a new {@link LNBitsClient} with fresh instances of each
     * sub client.
     */
    public LNBitsClient() {
        this.users = new LNBitsUsers();
        this.wallets = new LNBitsWallets();
        this.payments = new LNBitsPayments();
        this.utils = new LNBitsUtils();
        this.cache = new LNBitsCacheUsers();
    }

    // === Accessor methods for sub-clients ===

    /**
     * Access the user related LNBits operations.
     *
     * @return client for user API endpoints
     */
    public LNBitsUsers users() {
        return users;
    }

    /**
     * Access the wallet related LNBits operations.
     *
     * @return client for wallet API endpoints
     */
    public LNBitsWallets wallets() {
        return wallets;
    }

    /**
     * Access the payments related LNBits operations.
     *
     * @return client for payment API endpoints
     */
    public LNBitsPayments payments() {
        return payments;
    }

    /**
     * Access various utility helpers for the LNBits API.
     *
     * @return utility helper instance
     */
    public LNBitsUtils utils() {
        return utils;
    }

    /**
     * Access the LNBits user+wallet cache layer.
     *
     * @return cache layer for all known users and wallets
     */
    public LNBitsCacheUsers cache() {
        return cache;
    }
} 
