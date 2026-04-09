package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Legacy corruption admin handler. Now a no-op passthrough.
 * All corruption actions are handled by CorruptionHandler.java.
 */
public class CorruptionAdminHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        return false;
    }
}
