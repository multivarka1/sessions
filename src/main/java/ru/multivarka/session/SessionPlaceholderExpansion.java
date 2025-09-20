package ru.multivarka.session;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Locale;

public class SessionPlaceholderExpansion extends PlaceholderExpansion {
    private final Session plugin;
    private final PlayerSessionManager manager;

    public SessionPlaceholderExpansion(Session plugin, PlayerSessionManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "session";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        Duration elapsed = manager.getElapsed(player.getUniqueId());
        if (elapsed == null) {
            return "";
        }

        String param = params.toLowerCase(Locale.ROOT);
        switch (param) {
            case "seconds_played":
                return Integer.toString((int) (elapsed.getSeconds() % 60));
            case "minutes_played":
                return Integer.toString((int) ((elapsed.toMinutes()) % 60));
            case "hours_played":
                return Integer.toString((int) ((elapsed.toHours()) % 24));
            case "days_played":
                return Long.toString(elapsed.toDays());
            case "time_played":
                return formatFull(elapsed);
            case "time_played:seconds":
                return Long.toString(elapsed.getSeconds());
            case "time_played:minutes":
                return Long.toString(elapsed.toMinutes());
            case "time_played:hours":
                return Long.toString(elapsed.toHours());
            case "time_played:days":
                return Long.toString(elapsed.toDays());
            default:
                return null;
        }
    }

    private String formatFull(Duration duration) {
        long totalSeconds = duration.getSeconds();
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = (totalSeconds / 3600) % 24;
        long days = totalSeconds / 86400;
        return String.format("%d:%02d:%02d:%02d", days, hours, minutes, seconds);
    }
}
