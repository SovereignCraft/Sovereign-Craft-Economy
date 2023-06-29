package com.sovereigncraft.economy.commands;

import com.sovereigncraft.economy.SCEconomy;
import com.sovereigncraft.economy.util.MapCreator;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.io.File;

public class getMapInterface implements CommandExecutor {

    @Override
    @SneakyThrows
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String prefix = SCEconomy.getMessage("messages.prefix");

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix+SCEconomy.getMessage("messages.onlyForPlayers"));
            return true;
        }

        if (args.length > 0) {
            return false;
        }

        Player player = (Player) sender;
            if (player.getItemInHand().getType() != Material.AIR) {
                sender.sendMessage(prefix + SCEconomy.getMessage("messages.handNotEmpty"));
                return true;
            }
        if (SCEconomy.getInstance().getConfig().get("interfaceID") == null) {
            ItemStack map = MapCreator.generatePlayerMap(player);
            Integer id = ((MapMeta) map.getItemMeta()).getMapId();
            player.setItemInHand(map);
            File configFile = new File(SCEconomy.getInstance().getDataFolder()+File.separator+"config.yml");
            FileConfiguration config = SCEconomy.getInstance().getConfig();
            config.set("interfaceID",id);
            config.save(configFile);

        } else {
            ItemStack map = MapCreator.clonePlayerMap();
            player.setItemInHand(map);
        }
        sender.sendMessage(prefix + SCEconomy.getMessage("messages.success"));
        return true;
    }

}
