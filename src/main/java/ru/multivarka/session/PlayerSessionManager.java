package ru.multivarka.session;

import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSessionManager {
    private final Map<UUID, Long> sessionStart = new ConcurrentHashMap<>();
    private final Map<UUID, Long> carriedElapsed;

    public PlayerSessionManager(Map<UUID, Long> carriedElapsed) {
        this.carriedElapsed = new ConcurrentHashMap<>(carriedElapsed);
    }

    public void startSession(Player player) {
        UUID uuid = player.getUniqueId();
        long resumeElapsed = Optional.ofNullable(carriedElapsed.remove(uuid)).orElse(0L);
        long now = System.currentTimeMillis();
        sessionStart.put(uuid, now - resumeElapsed);
    }

    public void endSession(Player player) {
        sessionStart.remove(player.getUniqueId());
    }

    public Duration getElapsed(Player player) {
        return getElapsed(player.getUniqueId());
    }

    public Duration getElapsed(UUID uuid) {
        Long start = sessionStart.get(uuid);
        if (start == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        return Duration.ofMillis(Math.max(0, now - start));
    }

    public Map<UUID, Long> snapshotElapsed() {
        Map<UUID, Long> snapshot = new ConcurrentHashMap<>();
        long now = System.currentTimeMillis();
        sessionStart.forEach((uuid, start) -> {
            long elapsed = Math.max(0, now - start);
            snapshot.put(uuid, elapsed);
        });
        return snapshot;
    }
}
