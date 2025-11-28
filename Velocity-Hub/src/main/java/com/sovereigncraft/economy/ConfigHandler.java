package com.sovereigncraft.economy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigHandler {

    private final Properties properties;
    private final Path dataDirectory;

    public ConfigHandler(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.properties = new Properties();
        loadConfig();
    }

    private void loadConfig() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
            File configFile = new File(dataDirectory.toFile(), "velocity-hub.properties");
            if (!configFile.exists()) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("velocity-hub.properties")) {
                    if (in != null) {
                        Files.copy(in, configFile.toPath());
                    } else {
                        configFile.createNewFile();
                    }
                }
            }
            try (InputStream in = Files.newInputStream(configFile.toPath())) {
                properties.load(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHost() {
        return properties.getProperty("host");
    }

    public String getPort() {
        return properties.getProperty("port");
    }

    public String getAccessToken() {
        return properties.getProperty("accessToken");
    }

    public String getAdminKey() {
        return properties.getProperty("AdminKey");
    }

    public String getBedrockPrefix() {
        return properties.getProperty("bedrockPrefix");
    }

    public String getLNBitsBedrockSuffix() {
        return properties.getProperty("lnbitsbedrocksuffix", ".b");
    }
}