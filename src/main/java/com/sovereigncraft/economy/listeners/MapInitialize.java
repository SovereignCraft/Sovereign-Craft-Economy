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
        /*File qrbgfile = new File(Main.getInstance().getDataFolder()+File.separator+"qrbg.png");
        BufferedImage qrbg = ImageIO.read(qrbgfile);
        mapView.addRenderer(new MapRenderer() {
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                mapCanvas.drawImage(0, 0, qrbg);
            }
        });
        int qrloc = (128 - image.getWidth())/2;
        mapView.addRenderer(new MapRenderer() {
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                mapCanvas.drawImage(qrloc, qrloc, image);
            }
        });
        File qrwmfile = new File(Main.getInstance().getDataFolder()+File.separator+"qrwm.png");
        BufferedImage qrwm = ImageIO.read(qrwmfile);
        int wmloc = (128 - qrwm.getWidth())/2;
        mapView.addRenderer(new MapRenderer() {
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                mapCanvas.drawImage(wmloc, wmloc, qrwm);
            }
        }); */
    }

}
