package com.sovereigncraft.economy.util;

public class QRData {
    public final String qrData;
    public final String text;
    public boolean paid;

    public QRData(String qrData, String text) {
        this.qrData = qrData;
        this.text = text;
        this.paid = false;
    }
}
