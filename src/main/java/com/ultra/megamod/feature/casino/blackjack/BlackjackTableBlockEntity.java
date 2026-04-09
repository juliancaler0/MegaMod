package com.ultra.megamod.feature.casino.blackjack;

import com.ultra.megamod.feature.casino.CasinoRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlackjackTableBlockEntity extends BlockEntity {

    /**
     * Static tick method called by the block's ticker.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, BlackjackTableBlockEntity be) {
        if (level.isClientSide()) return;
        if (be.gameInstance != null) {
            be.gameInstance.tick();
        }
    }

    // Static tracking: player UUID -> table BlockPos (for disconnect cleanup)
    private static final Map<UUID, BlockPos> SEATED_PLAYERS = new HashMap<>();

    // Client-side rendering state (updated by BlackjackSyncPayload handler)
    public static volatile String clientGameState = null;

    /** Transient game instance -- not persisted across server restarts. */
    @Nullable
    private BlackjackTable gameInstance;

    public BlackjackTableBlockEntity(BlockPos pos, BlockState state) {
        super(CasinoRegistry.BLACKJACK_TABLE_BE.get(), pos, state);
    }

    public BlackjackTable getOrCreateGame() {
        if (gameInstance == null) {
            gameInstance = new BlackjackTable(this.worldPosition);
        }
        return gameInstance;
    }

    @Nullable
    public BlackjackTable getGame() {
        return gameInstance;
    }

    public static void trackPlayer(UUID playerId, BlockPos tablePos) {
        SEATED_PLAYERS.put(playerId, tablePos);
    }

    public static void untrackPlayer(UUID playerId) {
        SEATED_PLAYERS.remove(playerId);
    }

    public static BlockPos getTableForPlayer(UUID playerId) {
        return SEATED_PLAYERS.get(playerId);
    }

    public static int getSeatedPlayerCount() {
        return SEATED_PLAYERS.size();
    }
}
