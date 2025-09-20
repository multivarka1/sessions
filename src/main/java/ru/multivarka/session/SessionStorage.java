package ru.multivarka.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class SessionStorage {
    private static final Type TYPE = new TypeToken<Map<String, Long>>() {}.getType();

    private final Path dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;

    public SessionStorage(JavaPlugin plugin) {
        Path dataFolder = plugin.getDataFolder().toPath();
        this.dataFile = dataFolder.resolve("sessions.json");
        this.logger = plugin.getLogger();
    }

    public Map<UUID, Long> load() {
        if (Files.notExists(dataFile)) {
            return new HashMap<>();
        }

        try (Reader reader = Files.newBufferedReader(dataFile)) {
            Map<String, Long> raw = gson.fromJson(reader, TYPE);
            Map<UUID, Long> result = new HashMap<>();
            if (raw == null) {
                return result;
            }

            for (Map.Entry<String, Long> entry : raw.entrySet()) {
                try {
                    UUID uuid = UUID.fromString(entry.getKey());
                    Long elapsed = entry.getValue();
                    if (elapsed != null && elapsed >= 0) {
                        result.put(uuid, elapsed);
                    }
                } catch (IllegalArgumentException ignored) {
                    logger.warning("Skipping malformed UUID in sessions.json: " + entry.getKey());
                }
            }
            return result;
        } catch (IOException ex) {
            logger.warning("Failed to load session data: " + ex.getMessage());
            return new HashMap<>();
        }
    }

    public void save(Map<UUID, Long> sessions) {
        try {
            Files.createDirectories(dataFile.getParent());
        } catch (IOException ex) {
            logger.warning("Failed to create plugin data folder: " + ex.getMessage());
            return;
        }

        Map<String, Long> raw = new HashMap<>();
        for (Map.Entry<UUID, Long> entry : sessions.entrySet()) {
            raw.put(entry.getKey().toString(), entry.getValue());
        }

        try (Writer writer = Files.newBufferedWriter(dataFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            gson.toJson(raw, TYPE, writer);
        } catch (IOException ex) {
            logger.warning("Failed to save session data: " + ex.getMessage());
        }
    }
}
