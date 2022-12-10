package com.sovereigncraft.economy.eco;

import com.sovereigncraft.economy.SCEconomy;
import com.sovereigncraft.economy.ConfigHandler;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

public class VaultImpl implements net.milkbowl.vault.economy.Economy {

	@Override
	public boolean isEnabled() {
		return SCEconomy.getInstance() != null;
	}
	
	@Override
	public String currencyNamePlural() {
		return "Sats";
	}

	@Override
	public String currencyNameSingular() {
		return "Satoshi";
	}

	@Override
	public String format(double v) {
		BigDecimal bd = new BigDecimal(v).setScale(0, RoundingMode.HALF_EVEN);
        return String.valueOf(bd.doubleValue());

	}

	@Override
	public int fractionalDigits() {
		return -1;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean createPlayerAccount(String name) {
		return createAccount(Bukkit.getOfflinePlayer(name).getUniqueId());
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer player) {
		return createAccount(player.getUniqueId());
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean createPlayerAccount(String name, String world) {
		return createAccount(Bukkit.getOfflinePlayer(name).getUniqueId());
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer player, String world) {
		return createAccount(player.getUniqueId());
	}
	
	private boolean createAccount(UUID uuid) {
		return SCEconomy.createAccount(uuid);
	}

	@SuppressWarnings("deprecation")
	@Override
	public EconomyResponse depositPlayer(String name, double amount) {
		return deposit(Bukkit.getOfflinePlayer(name).getUniqueId(), amount);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
		return deposit(player.getUniqueId(), amount);
	}

	@SuppressWarnings("deprecation")
	@Override
	public EconomyResponse depositPlayer(String name, String world, double amount) {
		return deposit(Bukkit.getOfflinePlayer(name).getUniqueId(), amount);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, String world, double amount) {
		return deposit(player.getUniqueId(), amount);
	}
	
	private EconomyResponse deposit(UUID uuid, double amount) {
		if (!SCEconomy.deposit(uuid, amount)) {
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Failed to deposit funds.");
		}
		return new EconomyResponse(amount, getBalance(uuid), ResponseType.SUCCESS, "");
	}

	@SuppressWarnings("deprecation")
	@Override
	public double getBalance(String name) {
		return getBalance(Bukkit.getOfflinePlayer(name).getUniqueId());
	}

	@Override
	public double getBalance(OfflinePlayer player) {
		return getBalance(player.getUniqueId());
	}

	@SuppressWarnings("deprecation")
	@Override
	public double getBalance(String name, String world) {
		return getBalance(Bukkit.getOfflinePlayer(name).getUniqueId());
	}

	@Override
	public double getBalance(OfflinePlayer player, String world) {
		return getBalance(player.getUniqueId());
	}
	
	private double getBalance(UUID uuid) {
		return SCEconomy.getBalance(uuid).getBalance();
	}

	@Override
	public String getName() {
		return "Economy";
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean has(String name, double amount) {
		return has(Bukkit.getOfflinePlayer(name).getUniqueId(), amount);
	}

	@Override
	public boolean has(OfflinePlayer player, double amount) {
		return has(player.getUniqueId(), amount);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean has(String name, String world, double amount) {
		return has(Bukkit.getOfflinePlayer(name).getUniqueId(), amount);
	}

	@Override
	public boolean has(OfflinePlayer player, String world, double amount) {
		return has(player.getUniqueId(), amount);
	}
	
	private boolean has(UUID uuid, double amount) {
		return SCEconomy.has(uuid, amount);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean hasAccount(String name) {
		return hasAccount(Bukkit.getOfflinePlayer(name).getUniqueId());
	}

	@Override
	public boolean hasAccount(OfflinePlayer player) {
		return hasAccount(player.getUniqueId());
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean hasAccount(String name, String world) {
		return hasAccount(Bukkit.getOfflinePlayer(name).getUniqueId());
	}

	@Override
	public boolean hasAccount(OfflinePlayer player, String world) {
		return hasAccount(player.getUniqueId());
	}
	
	private boolean hasAccount(UUID uuid) {
		return SCEconomy.hasAccount(uuid);
	}

	@SuppressWarnings("deprecation")
	@Override
	public EconomyResponse withdrawPlayer(String name, double amount) {
		return withdraw(Bukkit.getOfflinePlayer(name).getUniqueId(), amount);
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
		return withdraw(player.getUniqueId(), amount);
	}

	@SuppressWarnings("deprecation")
	@Override
	public EconomyResponse withdrawPlayer(String name, String world, double amount) {
		return withdraw(Bukkit.getOfflinePlayer(name).getUniqueId(), amount);
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer player, String world, double amount) {
		return withdraw(player.getUniqueId(), amount);
	}
	
	private EconomyResponse withdraw(UUID uuid, double amount) {
		if (!SCEconomy.withdraw(uuid, amount)) {
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Failed to withdraw funds.");
		}
		return new EconomyResponse(amount, getBalance(uuid), ResponseType.SUCCESS, "");
	}

	@Override
	public boolean hasBankSupport() {
		return false;
	}

	@Override
	public List<String> getBanks() {
		return null;
	}

	@Override
	public EconomyResponse isBankMember(String arg0, String arg1) {
		return null;
	}

	@Override
	public EconomyResponse isBankMember(String arg0, OfflinePlayer arg1) {
		return null;
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, String arg1) {
		return null;
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, OfflinePlayer arg1) {
		return null;
	}
	
	@Override
	public EconomyResponse bankBalance(String arg0) {
		return null;
	}

	@Override
	public EconomyResponse bankDeposit(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse bankHas(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse bankWithdraw(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse createBank(String arg0, String arg1) {
		return null;
	}

	@Override
	public EconomyResponse createBank(String arg0, OfflinePlayer arg1) {
		return null;
	}

	@Override
	public EconomyResponse deleteBank(String arg0) {
		return null;
	}

}
