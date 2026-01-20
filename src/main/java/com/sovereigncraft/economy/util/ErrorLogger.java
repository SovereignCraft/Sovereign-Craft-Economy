package com.sovereigncraft.economy.util;

import com.sovereigncraft.economy.SCEconomy;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class ErrorLogger {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_FILE_NAME = "errors.log";

    public static void log(String context, Throwable throwable) {
        log(context, null, throwable);
    }

    public static void log(String context, String extraInfo, Throwable throwable) {
        // Log a brief message to the console
        String consoleMessage = "[Sovereign-Craft-Economy] Error in " + context + (extraInfo != null ? ": " + extraInfo : "");
        Bukkit.getLogger().log(Level.WARNING, consoleMessage + " (See errors.log for details)");

        // Log detailed info to file
        File dataFolder = SCEconomy.getInstance().getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File logFile = new File(dataFolder, LOG_FILE_NAME);
        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println("=== " + DATE_FORMAT.format(new Date()) + " ===");
            pw.println("Context: " + context);
            if (extraInfo != null) {
                pw.println("Info: " + extraInfo);
            }
            if (throwable != null) {
                throwable.printStackTrace(pw);
            }
            pw.println("========================================");
            pw.println();

        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not write to error log file!", e);
            if (throwable != null) {
                throwable.printStackTrace();
            }
        }
    }
}
