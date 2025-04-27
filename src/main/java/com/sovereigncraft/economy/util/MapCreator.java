package com.sovereigncraft.economy.util;

import com.sovereigncraft.economy.SCEconomy;
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
    public static ItemStack generateMap(String string, Player p, String data,Boolean perPlayer) {

        ItemStack itemStack = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
        MapView mapView = Bukkit.createMap(p.getWorld());
        mapView.setScale(MapView.Scale.CLOSEST);
        mapView.setUnlimitedTracking(true);
        mapView.getRenderers().clear();
        TemplateCreator templateCreator = new TemplateCreator();
        templateCreator.createTemplate(string,mapView);


        mapMeta.setMapView(mapView); // This approach works for legacy versions like 1.8 to 1.12.
        itemStack.setItemMeta(mapMeta);

        if (SCEconomy.getInstance().getConfig().getBoolean("settings.changeMapName")) {
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(data);
            itemStack.setItemMeta(meta);
        }


        return itemStack;
    }
    public static ItemStack clonePlayerMap() {
        ItemStack itemStack = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
        MapView mapView = Bukkit.getMap(SCEconomy.getInstance().getConfig().getInt("interfaceID"));
        /*
            this error:
                incompatible types: possible lossy conversion from int to short
            happens because Bukkit.getMap(int id) expects a short (16-bit integer), but getInt("interfaceID") returns an int (32-bit integer). Java doesn’t automatically convert int to short without an explicit cast.
         */
        mapMeta.setMapView(mapView); // This approach works for legacy versions like 1.8 to 1.12.
        itemStack.setItemMeta(mapMeta);

        if (SCEconomy.getInstance().getConfig().getBoolean("settings.changeMapName")) {
            ItemMeta meta = itemStack.getItemMeta();
            itemStack.setItemMeta(meta);
        }


        return itemStack;
    }
    public static ItemStack generatePlayerMap(Player p) {

        ItemStack itemStack = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
        MapView mapView = Bukkit.createMap(p.getWorld());
        mapView.setScale(MapView.Scale.CLOSEST);
        mapView.setUnlimitedTracking(true);
        mapView.getRenderers().clear();
        TemplateCreator templateCreator = new TemplateCreator();
        templateCreator.createPlayerTemplate(mapView);

        mapMeta.setMapView(mapView); // This approach works for legacy versions like 1.8 to 1.12.
        itemStack.setItemMeta(mapMeta);

        if (SCEconomy.getInstance().getConfig().getBoolean("settings.changeMapName")) {
            ItemMeta meta = itemStack.getItemMeta();
            itemStack.setItemMeta(meta);
        }


        return itemStack;
    }
}
