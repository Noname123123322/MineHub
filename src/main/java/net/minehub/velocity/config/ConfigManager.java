package net.minehub.velocity.config;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final Path dataDirectory;
    private final Path configFile;
    private Map<String, Object> config;

    public ConfigManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.configFile = dataDirectory.resolve("config.yml");
    }

    public void loadConfig() throws IOException {
        // Create data directory if it doesn't exist
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
        }

        // Create default config if it doesn't exist
        if (!Files.exists(configFile)) {
            createDefaultConfig();
        }

        // Load config from file
        Yaml yaml = new Yaml();
        try (FileInputStream fis = new FileInputStream(configFile.toFile())) {
            config = yaml.load(fis);
        }

        if (config == null) {
            config = new HashMap<>();
        }
    }

    private void createDefaultConfig() throws IOException {
        Map<String, Object> defaultConfig = new HashMap<>();

        // Database configuration
        Map<String, Object> database = new HashMap<>();
        database.put("host", "localhost");
        database.put("port", 3306);
        database.put("database", "minehub");
        database.put("username", "minehub");
        database.put("password", "password");
        database.put("pool-size", 10);
        defaultConfig.put("database", database);

        // Server configuration
        Map<String, Object> server = new HashMap<>();
        server.put("cleanup-interval-hours", 72);
        server.put("max-servers-per-user", 5);
        server.put("default-hub-server", "lobby");
        defaultConfig.put("server", server);

        // Messages configuration
        Map<String, Object> messages = new HashMap<>();
        messages.put("server-added", "&aServer ''{0}'' has been added to the network!");
        messages.put("server-removed", "&cServer ''{0}'' has been removed from the network!");
        messages.put("server-offline", "&cServer ''{0}'' is currently offline!");
        messages.put("no-permission", "&cYou don't have permission to use this command!");
        messages.put("server-not-found", "&cServer ''{0}'' not found!");
        messages.put("max-servers-reached", "&cYou have reached the maximum number of servers ({0})!");
        defaultConfig.put("messages", messages);

        // GUI configuration
        Map<String, Object> gui = new HashMap<>();
        gui.put("title", "&6MineHub Server Selector");
        gui.put("size", 54);
        gui.put("online-server-item", "LIME_STAINED_GLASS_PANE");
        gui.put("offline-server-item", "RED_STAINED_GLASS_PANE");
        defaultConfig.put("gui", gui);

        // Write default config
        Yaml yaml = new Yaml();
        try (FileWriter writer = new FileWriter(configFile.toFile())) {
            yaml.dump(defaultConfig, writer);
        }

        config = defaultConfig;
    }

    public String getString(String path) {
        return getString(path, "");
    }

    public String getString(String path, String defaultValue) {
        Object value = getNestedValue(path);
        return value != null ? value.toString() : defaultValue;
    }

    public int getInt(String path) {
        return getInt(path, 0);
    }

    public int getInt(String path, int defaultValue) {
        Object value = getNestedValue(path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        Object value = getNestedValue(path);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    private Object getNestedValue(String path) {
        String[] keys = path.split("\\.");
        Object current = config;

        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(key);
            } else {
                return null;
            }
        }

        return current;
    }

    public Map<String, Object> getConfig() {
        return config;
    }
}