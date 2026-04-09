package com.ultra.megamod.feature.combat.client;

import com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass;

/**
 * Client-side cache of the local player's class selection.
 * Updated by {@link com.ultra.megamod.feature.combat.network.ClassSyncPayload}
 * whenever the server sends a class update (login, class choice, admin change).
 */
public final class ClientClassCache {

    private ClientClassCache() {}

    /** The local player's current class. Defaults to NONE until synced. */
    private static volatile PlayerClass playerClass = PlayerClass.NONE;

    public static PlayerClass getPlayerClass() {
        return playerClass;
    }

    public static void setPlayerClass(PlayerClass cls) {
        playerClass = cls != null ? cls : PlayerClass.NONE;
    }

    /** Reset on disconnect. */
    public static void reset() {
        playerClass = PlayerClass.NONE;
    }
}
