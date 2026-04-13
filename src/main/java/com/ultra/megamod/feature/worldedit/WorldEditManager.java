package com.ultra.megamod.feature.worldedit;

import com.ultra.megamod.feature.worldedit.session.LocalSession;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static singleton managing per-player LocalSession instances.
 * Sessions are kept in-memory only (not persisted to disk) — WorldEdit
 * sessions are expected to end when the player logs out.
 */
public final class WorldEditManager {

    private static final Map<UUID, LocalSession> SESSIONS = new ConcurrentHashMap<>();

    private WorldEditManager() {}

    public static LocalSession getSession(ServerPlayer player) {
        return SESSIONS.computeIfAbsent(player.getUUID(), LocalSession::new);
    }

    public static LocalSession getSessionOrNull(UUID id) {
        return SESSIONS.get(id);
    }

    public static void removeSession(UUID id) {
        SESSIONS.remove(id);
    }

    public static Map<UUID, LocalSession> allSessions() {
        return SESSIONS;
    }
}
