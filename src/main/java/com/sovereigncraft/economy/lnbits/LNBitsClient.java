package com.sovereigncraft.economy.lnbits;

public class LNBitsClient {

    // Root client references for domain-specific operations
    private final LNBitsUsers users;
    private final LNBitsWallets wallets;
    private final LNBitsPayments payments;
    private final LNBitsUtils utils;

    // Constructor
    public LNBitsClient() {
        this.users = new LNBitsUsers();
        this.wallets = new LNBitsWallets();
        this.payments = new LNBitsPayments();
        this.utils = new LNBitsUtils();
    }

    // === Accessor methods for sub-clients ===
    public LNBitsUsers users() {
        return users;
    }

    public LNBitsWallets wallets() {
        return wallets;
    }

    public LNBitsPayments payments() {
        return payments;
    }

    public LNBitsUtils utils() {
        return utils;
    }
}
