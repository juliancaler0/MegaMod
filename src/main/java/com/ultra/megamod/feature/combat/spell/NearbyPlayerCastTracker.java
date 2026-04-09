package com.ultra.megamod.feature.combat.spell;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side tracker for nearby players' casting state.
 * Updated by {@link NearbyPlayerCastPayload} from the server.
 * Read by {@link SpellCastOverlay} to render cast indicators above other players' heads.
 */
public final class NearbyPlayerCastTracker {

    private NearbyPlayerCastTracker() {}

    public record CastState(String spellName, float progress, int schoolColor, long updateTime) {}

    /** Active casters near the local player. Keyed by caster UUID. */
    private static final Map<UUID, CastState> ACTIVE_CASTERS = new ConcurrentHashMap<>();

    public static void updateCasterState(UUID casterId, boolean casting, String spellName, float progress, int schoolColor) {
        if (casting) {
            ACTIVE_CASTERS.put(casterId, new CastState(spellName, progress, schoolColor, System.currentTimeMillis()));
        } else {
            ACTIVE_CASTERS.remove(casterId);
        }
    }

    /** Returns all currently-casting nearby players (excludes local player). */
    public static Map<UUID, CastState> getActiveCasters() {
        return ACTIVE_CASTERS;
    }

    /** Remove stale entries (older than 2 seconds without update). */
    public static void tick() {
        long now = System.currentTimeMillis();
        ACTIVE_CASTERS.entrySet().removeIf(e -> now - e.getValue().updateTime() > 2000);
    }

    /** Clear all state (on disconnect). */
    public static void reset() {
        ACTIVE_CASTERS.clear();
    }
}
