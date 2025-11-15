package com.sovereigncraft.economy.util;

import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import com.sovereigncraft.economy.SCEconomy;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.awt.image.BufferedImage;
import java.io.File;

public class QRCreator {

    String data;

    public QRCreator(String data) {
        this.data = data;
    }
    @SneakyThrows
    public void generate(String string, Player player) {

        ItemStack map = MapCreator.generateMap(string, player, data, false);

        player.setItemInHand(map);
        String id = String.valueOf(((MapMeta) map.getItemMeta()).getMapId());

        File mapsData = new File(SCEconomy.getInstance().getDataFolder()+File.separator+"data.yml");
        FileConfiguration maps = YamlConfiguration.loadConfiguration(mapsData);
        maps.set(id, data);
        maps.save(mapsData);

    }
    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        int[] rowPixels = new int[width];
        BitArray row = new BitArray(width);
        for (int y = 0; y < height; y++) {
            row = matrix.getRow(y, row);
            for (int x = 0; x < width; x++) {
                rowPixels[x] = row.get(x) ? 0xFF000000 : 0xFFFFFFFF;
            }
            image.setRGB(0, y, width, 1, rowPixels, 0, width);
        }
        return image;
    }

    @SneakyThrows
    public static BufferedImage generateQRcode(String data) {
        QRCode qrCode = Encoder.encode(data, ErrorCorrectionLevel.Q);
        BitMatrix matrix = convertByteMatrixToBitMatrix(qrCode.getMatrix());
        BufferedImage image = toBufferedImage(matrix);
        return image;
    }

    private static BitMatrix convertByteMatrixToBitMatrix(ByteMatrix byteMatrix) {
        int width = byteMatrix.getWidth();
        int height = byteMatrix.getHeight();
        int borderSize = 4;  // Set the border size
        BitMatrix bitMatrix = new BitMatrix(width + 2 * borderSize, height + 2 * borderSize);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (byteMatrix.get(i, j) == 1) {
                    bitMatrix.set(i + borderSize, j + borderSize);  // Offset by the border size
                }
            }
        }

        return bitMatrix;
    }




}
