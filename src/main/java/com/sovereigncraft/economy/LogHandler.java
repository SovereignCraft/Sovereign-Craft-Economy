package com.sovereigncraft.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class LogHandler {
    private static final File logFile = new File(SCEconomy.getInstance().getDataFolder(), "logs/actions.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Initialize log file directory
    public static void initialize() {
        File dir = logFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
                writeLog(new ArrayList<>());  // Start with empty log array
            } catch (IOException e) {
                Bukkit.getLogger().warning("Unable to create actions.json log file.");
            }
        }
    }

    // Write a new log entry
    public static void logAction(String action, Map<String, Object> details) {
        try {
            List<Map<String, Object>> logs = readLog();
            Map<String, Object> logEntry = new LinkedHashMap<>();
            logEntry.put("timestamp", LocalDateTime.now().toString());
            logEntry.put("action", action);
            logEntry.put("details", details);
            logs.add(logEntry);
            writeLog(logs);
        } catch (IOException e) {
            Bukkit.getLogger().warning("Unable to write to actions.json: " + e.getMessage());
        }
    }

    // Read current log file into a List
    private static List<Map<String, Object>> readLog() throws IOException {
        Scanner scanner = new Scanner(logFile);
        StringBuilder json = new StringBuilder();
        while (scanner.hasNextLine()) {
            json.append(scanner.nextLine());
        }
        scanner.close();

        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        Map[] parsedLogs = gson.fromJson(json.toString(), Map[].class);
        return new ArrayList<>(Arrays.asList(parsedLogs));
    }

    // Write log list back to file
    private static void writeLog(List<Map<String, Object>> logs) throws IOException {
        FileWriter writer = new FileWriter(logFile);
        gson.toJson(logs, writer);
        writer.flush();
        writer.close();
    }
}
