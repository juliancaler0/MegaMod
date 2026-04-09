package com.ultra.megamod.feature.marketplace.network;

import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.marketplace.block.TradingTerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

/**
 * Routes terminal_ prefixed actions from ComputerActionHandler to the correct
 * TradingTerminalBlockEntity. Searches for the terminal BE near the player.
 */
public class TradingTerminalHandler {

    private static final double SEARCH_RADIUS = 10.0;

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if (!action.startsWith("terminal_")) {
            return false;
        }

        UUID playerUuid = player.getUUID();

        // Find the trading terminal block entity that this player is part of
        TradingTerminalBlockEntity terminal = findPlayerTerminal(player, level);

        if (terminal == null) {
            // Not near any terminal — ignore silently
            return true;
        }

        return terminal.handleAction(player, action, jsonData, level, eco);
    }

    /**
     * Searches for a TradingTerminalBlockEntity near the player that they are part of.
     */
    private static TradingTerminalBlockEntity findPlayerTerminal(ServerPlayer player, ServerLevel level) {
        UUID playerUuid = player.getUUID();
        BlockPos playerPos = player.blockPosition();

        // Search in a box around the player
        int radius = (int) SEARCH_RADIUS;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof TradingTerminalBlockEntity terminal) {
                        if (terminal.isPartOfTrade(playerUuid)) {
                            return terminal;
                        }
                    }
                }
            }
        }

        return null;
    }
}
