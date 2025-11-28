package com.sovereigncraft.economy.util;

import com.sovereigncraft.economy.PaperSpoke;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static com.sovereigncraft.economy.util.QRCreator.generateQRcode;

public class TemplateCreator {

    private final PaperSpoke plugin;

    public TemplateCreator(PaperSpoke plugin) {
        this.plugin = plugin;
    }

    @SneakyThrows
    public void createPlayerTemplate(MapView mapView) {
        createDynBackground(mapView, true);
        createDynQR(mapView);
    }

    @SneakyThrows
    public void createTemplate(String data, MapView mapView) {
        createBackground(mapView, false);
        createQR(data, mapView, false);
        BufferedImage image = generateQRcode(data);
        if (image.getWidth() >= 80) {
            createWatermark(mapView, false);
        }
    }

    @SneakyThrows
    public void createBackground(MapView mapView, Boolean contextual) {
        File qrbgfile = new File(plugin.getDataFolder(), "qrbg.png");
        if (!qrbgfile.exists()) {
            plugin.saveResource("qrbg.png", false);
        }
        BufferedImage qrbg = ImageIO.read(qrbgfile);
        mapView.addRenderer(new MapRenderer(contextual) {
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                mapCanvas.drawImage(0, 0, qrbg);
            }
        });
    }

    @SneakyThrows
    public void createDynBackground(MapView mapView, Boolean contextual) {
        File qrbgfile = new File(plugin.getDataFolder(), "qrbgsc.png");
        if (!qrbgfile.exists()) {
            plugin.saveResource("qrbgsc.png", false);
        }
        BufferedImage qrbg = ImageIO.read(qrbgfile);
        mapView.addRenderer(new MapRenderer(contextual) {
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                mapCanvas.drawImage(0, 0, qrbg);
            }
        });
    }

    @SneakyThrows
    public void createWatermark(MapView mapView, Boolean contextual) {
        File qrwmfile = new File(plugin.getDataFolder(), "qrwm.png");
        if (!qrwmfile.exists()) {
            plugin.saveResource("qrwm.png", false);
        }
        BufferedImage qrwm = ImageIO.read(qrwmfile);
        int wmloc = (128 - qrwm.getWidth()) / 2;
        mapView.addRenderer(new MapRenderer(contextual) {

            @Override

            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                mapCanvas.drawImage(wmloc, wmloc, qrwm);
            }
        });
    }

    public void createQR(String data, MapView mapView, Boolean contextual) {
        BufferedImage image = generateQRcode(data);
        int qrloc = (128 - image.getWidth()) / 2;
        mapView.addRenderer(new MapRenderer(contextual) {
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                mapCanvas.drawImage(qrloc, qrloc, image);
            }
        });

    }

    public BufferedImage playerQR(Player player) {
        String pqr = plugin.playerQRInterface.get(player.getUniqueId());
        return generateQRcode(pqr);
    }

    @SneakyThrows
    public void createDynQR(MapView mapView) {
        mapView.addRenderer(new MapRenderer(true) {
            File qrbgfile = new File(plugin.getDataFolder(), "qrbgsc.png");
            BufferedImage qrbg = ImageIO.read(qrbgfile);
            File qrwmfile = new File(plugin.getDataFolder(), "qrwm.png");
            BufferedImage qrwm = ImageIO.read(qrwmfile);
            int wmloc = (128 - qrwm.getWidth()) / 2;

            @Override
            @SneakyThrows
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                mapCanvas.drawImage(0, 0, qrbg);
                String mapdata = plugin.playerQRInterface.get(player.getUniqueId());
                if (mapdata != null) {
                    BufferedImage image = playerQR(player);
                    int qrloc = (128 - image.getWidth()) / 2;
                    mapCanvas.drawImage(qrloc, qrloc, image);
                    if (image.getWidth() >= 80) {
                        mapCanvas.drawImage(wmloc, wmloc, qrwm);
                    }
                }
            }
        });
    }
}