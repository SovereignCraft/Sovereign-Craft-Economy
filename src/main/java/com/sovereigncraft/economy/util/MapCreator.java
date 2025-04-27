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


        // ===== BACKWARDS COMPATIBILITY: Support for Bukkit versions pre/post 1.20 =====
        // Versions before 1.20 use mapMeta.setMapView(MapView).
        // Versions 1.20+ removed setMapView(MapView) and introduced setMapId(short).
        // We use reflection to check if setMapView exists, otherwise fallback to setMapId.
        try {
            // Attempt to call legacy setMapView(MapView) (pre-1.20)
            mapMeta.getClass().getMethod("setMapView", MapView.class).invoke(mapMeta, mapView);
        } catch (NoSuchMethodException e) {
            // Fallback for newer Bukkit versions (1.20+)
            mapMeta.setMapId((short) mapView.getId());
        } catch (Exception e) {
            // Handle any reflection-related errors gracefully
            e.printStackTrace();
        }

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
        // ===== BACKWARDS COMPATIBILITY: Bukkit.getMap(short) expects short in 1.20+ =====
        // Explicit cast from int to short to avoid lossy conversion error
        int mapId = SCEconomy.getInstance().getConfig().getInt("interfaceID");
        MapView mapView;
        mapView = Bukkit.getMap((short) mapId);
        /*
            this error:
                incompatible types: possible lossy conversion from int to short
            happens because Bukkit.getMap(int id) expects a short (16-bit integer), but getInt("interfaceID") returns an int (32-bit integer). Java doesn’t automatically convert int to short without an explicit cast.
         */
        // ===== BACKWARDS COMPATIBILITY: Support for Bukkit versions pre/post 1.20 =====
        // Versions before 1.20 use mapMeta.setMapView(MapView).
        // Versions 1.20+ removed setMapView(MapView) and introduced setMapId(short).
        // We use reflection to check if setMapView exists, otherwise fallback to setMapId.
        try {
            // Attempt to call legacy setMapView(MapView) (pre-1.20)
            mapMeta.getClass().getMethod("setMapView", MapView.class).invoke(mapMeta, mapView);
        } catch (NoSuchMethodException e) {
            // Fallback for newer Bukkit versions (1.20+)
            mapMeta.setMapId((short) mapView.getId());
        } catch (Exception e) {
            // Handle any reflection-related errors gracefully
            e.printStackTrace();
        }
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

        // ===== BACKWARDS COMPATIBILITY: Support for Bukkit versions pre/post 1.20 =====
        // Versions before 1.20 use mapMeta.setMapView(MapView).
        // Versions 1.20+ removed setMapView(MapView) and introduced setMapId(short).
        // We use reflection to check if setMapView exists, otherwise fallback to setMapId.
        try {
            // Attempt to call legacy setMapView(MapView) (pre-1.20)
            mapMeta.getClass().getMethod("setMapView", MapView.class).invoke(mapMeta, mapView);
        } catch (NoSuchMethodException e) {
            // Fallback for newer Bukkit versions (1.20+)
            mapMeta.setMapId((short) mapView.getId());
        } catch (Exception e) {
            // Handle any reflection-related errors gracefully
            e.printStackTrace();
        }
        itemStack.setItemMeta(mapMeta);

        if (SCEconomy.getInstance().getConfig().getBoolean("settings.changeMapName")) {
            ItemMeta meta = itemStack.getItemMeta();
            itemStack.setItemMeta(meta);
        }


        return itemStack;
    }
}
