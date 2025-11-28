package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.PaperSpoke;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class BalanceCommand implements CommandExecutor {

    private final PaperSpoke plugin;
    private final Economy economy;

    public BalanceCommand(PaperSpoke plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can get their balance");
                return true;
            }

            Player player = (Player) sender;
            
            if (!economy.hasAccount(player)) {
                player.sendMessage("§cYou don't have an account.");
                return true;
            }
            
            plugin.getLogger().log(Level.INFO, "Checking balance for player: " + player.getName() + " UUID: " + player.getUniqueId().toString());
            try {
                sender.sendMessage(" §eYour balance is: §a" + economy.format(economy.getBalance(player)));
            } catch (Exception e) {
                sender.sendMessage("§cError retrieving balance. See console for details.");
                plugin.getLogger().log(Level.SEVERE, "Error getting balance for " + player.getName(), e);
            }
            return true;

        } else {
            sender.sendMessage("§cToo many arguments");
            return true;
        }
    }
}