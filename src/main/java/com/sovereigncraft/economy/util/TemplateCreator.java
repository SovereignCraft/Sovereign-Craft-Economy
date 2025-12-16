package com.sovereigncraft.economy.util;

import com.sovereigncraft.economy.SCEconomy;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.map.*;
import org.bukkit.map.MinecraftFont;
import org.bukkit.scheduler.BukkitRunnable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.sovereigncraft.economy.util.QRCreator.generateQRcode;

public class TemplateCreator {
    @SneakyThrows
    public void createPlayerTemplate (MapView mapView){
        //code needed to add the data to the hashmap
        createDynBackground(mapView, true);
        createDynQR(mapView);
    }
    @SneakyThrows
    public void createTemplate (String data, MapView mapView){
        createBackground(mapView, false);
        createQR(data,mapView,false);
        BufferedImage image = generateQRcode(data);
        if (image.getWidth() >= 80){
            createWatermark(mapView,false);
        }
    }
    @SneakyThrows
    public void createBackground(MapView mapView, Boolean contextual){
        File qrbgfile = new File(SCEconomy.getInstance().getDataFolder()+File.separator+"qrbg.png");
        BufferedImage qrbg = ImageIO.read(qrbgfile);
        mapView.addRenderer(new MapRenderer(contextual) {
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                mapCanvas.drawImage(0, 0, qrbg);
            }
        });
    }
    @SneakyThrows
    public void createDynBackground(MapView mapView, Boolean contextual){
        File qrbgfile = new File(SCEconomy.getInstance().getDataFolder()+File.separator+"qrbgsc.png");
        BufferedImage qrbg = ImageIO.read(qrbgfile);
        mapView.addRenderer(new MapRenderer(contextual) {
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                mapCanvas.drawImage(0, 0, qrbg);
            }
        });
    }
    @SneakyThrows
    public void createWatermark(MapView mapView,Boolean contextual){
        File qrwmfile = new File(SCEconomy.getInstance().getDataFolder()+File.separator+"qrwm.png");
        BufferedImage qrwm = ImageIO.read(qrwmfile);
        int wmloc = (128 - qrwm.getWidth())/2;
        mapView.addRenderer(new MapRenderer(contextual) {

            @Override

            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                mapCanvas.drawImage(wmloc, wmloc, qrwm);
            }
        });
    }
    public void createQR(String data, MapView mapView, Boolean contextual){
        BufferedImage image = generateQRcode(data);
        int qrloc = (128 - image.getWidth())/2;
        mapView.addRenderer(new MapRenderer(contextual) {
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                mapCanvas.drawImage(qrloc, qrloc, image);
            }
        });

    }
    public BufferedImage playerQR (Player player) {
        QRData pqr = SCEconomy.playerQRInterface.get(player.getUniqueId());
        return generateQRcode(pqr.qrData);
    }
    @SneakyThrows
    public void createDynQR(MapView mapView){
        //move buffered image in after a check of the hashmap
        mapView.addRenderer(new MapRenderer(true) {
            File qrbgfile = new File(SCEconomy.getInstance().getDataFolder()+File.separator+"qrbgsc.png");
            BufferedImage qrbg = ImageIO.read(qrbgfile);
            File qrwmfile = new File(SCEconomy.getInstance().getDataFolder()+File.separator+"qrwm.png");
            BufferedImage qrwm = ImageIO.read(qrwmfile);
            int wmloc = (128 - qrwm.getWidth())/2;
            @Override
            @SneakyThrows
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                mapCanvas.drawImage(0, 0, qrbg);
                QRData mapdata = SCEconomy.playerQRInterface.get(player.getUniqueId());
                if (mapdata != null) {
                    if (mapdata.paid) {
                        File paysuccessfile = new File(SCEconomy.getInstance().getDataFolder() + File.separator + "paysuccess.png");
                        BufferedImage paysuccess = ImageIO.read(paysuccessfile);
                        mapCanvas.drawImage(0, 0, paysuccess);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                SCEconomy.playerQRInterface.remove(player.getUniqueId());
                            }
                        }.runTaskLater(SCEconomy.getInstance(), 60);
                        return;
                    }
                    BufferedImage image = playerQR(player);
                    int qrloc = (128 - image.getWidth()) / 2;
                    mapCanvas.drawImage(qrloc, qrloc, image);
                    if(image.getWidth() >= 80){
                        mapCanvas.drawImage(wmloc, wmloc, qrwm);
                    }

                    String text = mapdata.text;
                    MinecraftFont font = MinecraftFont.Font;
                    List<String> lines = new ArrayList<>();
                    StringBuilder line = new StringBuilder();
                    String[] words = text.split(" ");
                    for (String word : words) {
                        String testString = line.length() > 0 ? line + " " + word : word;
                        if (font.getWidth(testString) > 124 && line.length() > 0) {
                            lines.add(line.toString());
                            line = new StringBuilder(word);
                        } else {
                            if (line.length() > 0) {
                                line.append(" ");
                            }
                            line.append(word);
                        }
                    }
                    lines.add(line.toString());

                    int numLines = lines.size();
                    int lineHeight = font.getHeight();
                    int totalTextHeight = numLines * lineHeight + (numLines > 1 ? numLines -1 : 0);
                    int startY = 127 - totalTextHeight;

                    // Draw background
                    for (int py = startY - 2; py < 127; py++) {
                        for (int px = 2; px < 126; px++) {
                            if (py >= 0 && py < 128) {
                                mapCanvas.setPixel(px, py, MapPalette.WHITE);
                            }
                        }
                    }

                    // Draw text
                    for (int i = 0; i < numLines; i++) {
                        String currentLine = lines.get(i);
                        int textWidth = font.getWidth(currentLine);
                        int x = (128 - textWidth) / 2;
                        mapCanvas.drawText(x, startY + i * (lineHeight + 1), font , currentLine);
                    }
                }
            }
        });

    }


}
