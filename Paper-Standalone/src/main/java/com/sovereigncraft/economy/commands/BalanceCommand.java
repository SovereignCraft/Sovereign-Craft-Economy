package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.LNBits;
import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class BalanceCommand implements org.bukkit.command.CommandExecutor {

    private final SCEconomy plugin;
    private final LNBits lnbits;

    public BalanceCommand(SCEconomy plugin) {
        this.plugin = plugin;
        this.lnbits = plugin.getLnbits();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can get their balance");
                return true;
            }

            Player player = (Player) sender;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!lnbits.hasAccount(player.getUniqueId())) {
                        player.sendMessage("§cYour wallet is not working.");
                        return;
                    }

                    sender.sendMessage(" §eYour balance is: §a" + lnbits.getBalanceString(player.getUniqueId()));
                }
            }.runTaskAsynchronously(plugin);
            return true;

        } else {
            sender.sendMessage("§cToo many arguments");

            return true;

        }


    }

}
