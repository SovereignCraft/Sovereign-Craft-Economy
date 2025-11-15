package com.sovereigncraft.economy.listeners;

import com.sovereigncraft.economy.SCEconomy;
import com.sovereigncraft.economy.util.TemplateCreator;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapView;

import java.io.File;

public class MapInitialize implements Listener {

    @SneakyThrows
    @EventHandler
    public void onMapInitialize(MapInitializeEvent e) {
        MapView mapView = e.getMap();
        if (mapView.getId() == SCEconomy.getInstance().getConfig().getInt("interfaceID")){
            mapView.getRenderers().clear();
            TemplateCreator templateCreator = new TemplateCreator();
            templateCreator.createPlayerTemplate(mapView);
        }
        File mapsData = new File(SCEconomy.getInstance().getDataFolder()+File.separator+"data.yml");
        FileConfiguration maps = YamlConfiguration.loadConfiguration(mapsData);
        String data = maps.getString(String.valueOf(mapView.getId()));
        if (data == null) return;
        mapView.getRenderers().clear();
        TemplateCreator templateCreator = new TemplateCreator();
        templateCreator.createTemplate(data,mapView);
    }

}
