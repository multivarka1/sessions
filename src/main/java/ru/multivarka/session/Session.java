package ru.multivarka.session;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public final class Session extends JavaPlugin {
    private SessionStorage storage;
    private PlayerSessionManager sessionManager;
    private SessionPlaceholderExpansion placeholderExpansion;

    @Override
    public void onEnable() {
        this.storage = new SessionStorage(this);
        Map<UUID, Long> carriedElapsed = storage.load();
        this.sessionManager = new PlayerSessionManager(carriedElapsed);

        getServer().getPluginManager().registerEvents(new SessionListener(this, sessionManager), this);

        Bukkit.getOnlinePlayers().forEach(sessionManager::startSession);

        registerPlaceholders();
    }

    @Override
    public void onDisable() {
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
            placeholderExpansion = null;
        }

        Bukkit.getOnlinePlayers().forEach(player -> {
            Duration elapsed = sessionManager.getElapsed(player);
            if (elapsed != null) {
                logSessionTime(player, elapsed);
            }
        });

        storage.save(sessionManager.snapshotElapsed());
    }

    void handleQuit(Player player) {
        Duration elapsed = sessionManager.getElapsed(player);
        if (elapsed != null) {
            logSessionTime(player, elapsed);
        }
        sessionManager.endSession(player);
    }

    private void registerPlaceholders() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.placeholderExpansion = new SessionPlaceholderExpansion(this, sessionManager);
            if (this.placeholderExpansion.register()) {
                getLogger().info("Registered PlaceholderAPI expansion");
            } else {
                getLogger().warning("Failed to register PlaceholderAPI expansion");
            }
        } else {
            getLogger().info("PlaceholderAPI not found, skipping placeholder registration");
        }
    }

    private void logSessionTime(Player player, Duration elapsed) {
        getLogger().info(player.getName() + " session time: " + formatDuration(elapsed));
    }

    String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 99) {
            hours = 99;
            minutes = 59;
            secs = 59;
        }

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
