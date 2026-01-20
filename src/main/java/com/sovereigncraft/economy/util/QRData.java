package com.sovereigncraft.economy.util;

public class QRData {
    public final String qrData;
    public final String text;
    public volatile boolean paid;
    public String id;
    public String type;
    public long lastCheck;
    public boolean removalScheduled;

    public QRData(String qrData, String text) {
        this.qrData = qrData;
        this.text = text;
        this.paid = false;
        this.id = null;
        this.type = null;
        this.lastCheck = 0;
        this.removalScheduled = false;
    }
    public QRData(String qrData, String text, String id, String type) {
        this.qrData = qrData;
        this.text = text;
        this.paid = false;
        this.id = id;
        this.type = type;
        this.lastCheck = 0;
        this.removalScheduled = false;
    }
}
