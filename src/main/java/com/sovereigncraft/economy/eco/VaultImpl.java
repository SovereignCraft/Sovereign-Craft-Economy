package com.sovereigncraft.economy.eco;

import com.sovereigncraft.economy.SCEconomy;
import com.sovereigncraft.economy.util.ErrorLogger;

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
		try {
			return SCEconomy.getEco().createWalletV1(uuid).isEmpty();
		} catch (Exception e) {
			String playerName = Bukkit.getOfflinePlayer(uuid).getName();
			ErrorLogger.log("createAccount", "Player: " + (playerName != null ? playerName : "Unknown") + " (" + uuid + ")", e);
			return false;
		}
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
		try {
			if (!SCEconomy.getEco().deposit(uuid, amount)) {
				return new EconomyResponse(0, 0, ResponseType.FAILURE, "Failed to deposit funds.");
			}
			return new EconomyResponse(amount, getBalance(uuid), ResponseType.SUCCESS, "");
		} catch (Exception e) {
			String playerName = Bukkit.getOfflinePlayer(uuid).getName();
			ErrorLogger.log("deposit", "Player: " + (playerName != null ? playerName : "Unknown") + " (" + uuid + "), Amount: " + amount, e);
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "An error occurred while depositing funds. Please contact an admin.");
		}
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
		try {
			return SCEconomy.getEco().getBalance(uuid);
		} catch (Exception e) {
			String playerName = Bukkit.getOfflinePlayer(uuid).getName();
			ErrorLogger.log("getBalance", "Player: " + (playerName != null ? playerName : "Unknown") + " (" + uuid + ")", e);
			return 0.0;
		}
	}

	@Override
	public String getName() {
		return "Sovereign-Craft-Economy";
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
		try {
			return SCEconomy.getEco().has(uuid, amount);
		} catch (Exception e) {
			String playerName = Bukkit.getOfflinePlayer(uuid).getName();
			ErrorLogger.log("has", "Player: " + (playerName != null ? playerName : "Unknown") + " (" + uuid + "), Amount: " + amount, e);
			return false;
		}
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
		try {
			return SCEconomy.getEco().hasAccount(uuid);
		} catch (Exception e) {
			String playerName = Bukkit.getOfflinePlayer(uuid).getName();
			ErrorLogger.log("hasAccount", "Player: " + (playerName != null ? playerName : "Unknown") + " (" + uuid + ")", e);
			return false;
		}
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
		try {
			if (!SCEconomy.getEco().withdraw(uuid, amount)) {
				return new EconomyResponse(0, 0, ResponseType.FAILURE, "Failed to withdraw funds.");
			}
			return new EconomyResponse(amount, getBalance(uuid), ResponseType.SUCCESS, "");
		} catch (Exception e) {
			String playerName = Bukkit.getOfflinePlayer(uuid).getName();
			ErrorLogger.log("withdraw", "Player: " + (playerName != null ? playerName : "Unknown") + " (" + uuid + "), Amount: " + amount, e);
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "An error occurred while withdrawing funds. Please contact an admin.");
		}
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
