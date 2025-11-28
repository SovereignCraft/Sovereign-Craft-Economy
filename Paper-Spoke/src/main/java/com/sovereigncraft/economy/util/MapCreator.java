package com.sovereigncraft.economy.util;

import com.sovereigncraft.economy.PaperSpoke;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

public class MapCreator {

    @SneakyThrows
    public static ItemStack generateMap(PaperSpoke plugin, String string, Player p, String data, Boolean perPlayer) {

        ItemStack itemStack = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
        MapView mapView = Bukkit.createMap(p.getWorld());
        mapView.setScale(MapView.Scale.CLOSEST);
        mapView.setUnlimitedTracking(true);
        mapView.getRenderers().clear();
        TemplateCreator templateCreator = new TemplateCreator(plugin);
        templateCreator.createTemplate(string, mapView);

        mapMeta.setMapView(mapView);
        itemStack.setItemMeta(mapMeta);

        if (plugin.getConfig().getBoolean("settings.changeMapName")) {
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(data);
            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    public static ItemStack clonePlayerMap(PaperSpoke plugin) {
        ItemStack itemStack = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
        MapView mapView = Bukkit.getMap(plugin.getConfig().getInt("interfaceID"));
        mapMeta.setMapView(mapView);
        itemStack.setItemMeta(mapMeta);

        if (plugin.getConfig().getBoolean("settings.changeMapName")) {
            ItemMeta meta = itemStack.getItemMeta();
            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    public static ItemStack generatePlayerMap(PaperSpoke plugin, Player p) {

        ItemStack itemStack = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
        MapView mapView = Bukkit.createMap(p.getWorld());
        mapView.setScale(MapView.Scale.CLOSEST);
        mapView.setUnlimitedTracking(true);
        mapView.getRenderers().clear();
        TemplateCreator templateCreator = new TemplateCreator(plugin);
        templateCreator.createPlayerTemplate(mapView);

        mapMeta.setMapView(mapView);
        itemStack.setItemMeta(mapMeta);

        if (plugin.getConfig().getBoolean("settings.changeMapName")) {
            ItemMeta meta = itemStack.getItemMeta();
            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }
}