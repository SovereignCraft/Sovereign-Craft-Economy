package com.sovereigncraft.economy;

import org.bukkit.plugin.java.JavaPlugin;
import com.sovereigncraft.economy.eco.*;

import java.io.IOException;
import java.util.Map;

public final class SCEconomy extends JavaPlugin {

    private static SCEconomy instance;
    private static VaultImpl vaultImpl;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public static SCEconomy getInstance() {
        return instance;
    }

    @Override
    public static boolean withdraw(String player, Integer amount) throws IOException, InterruptedException {
        String invoice = lnbits.createInvoice("server", amount);
        lnbits.processInvoice(player, invoice);

        return true;
    }
   @Override
   public static boolean deposit(String player, Integer amount) throws IOException, InterruptedException {
        String invoice = lnbits.createInvoice(player, amount);
        lnbits.processInvoice("server", invoice);
        return true;
    }
    @Override
    public static Double getBalance(String name) throws IOException, InterruptedException {
        Map map = lnbits.getWalletDetail(name);
        Double bal =  (Double) map.get("balance") / 1000;
        return bal;
    }
    @Override
    public static void createAccount(String name) throws IOException, InterruptedException {
        lnbits.createWallet(name);
    }
}
