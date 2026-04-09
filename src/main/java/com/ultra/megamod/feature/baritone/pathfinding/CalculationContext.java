package com.ultra.megamod.feature.baritone.pathfinding;

import com.ultra.megamod.feature.baritone.BotSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Context object passed to pathfinding calculations.
 * Contains references to the world, player, settings, mob avoidance, and tool caching.
 * Provides thread-safe block access via the snapshot in ServerBlockAccess.
 */
public class CalculationContext {
    public final ServerLevel level;
    public final ServerPlayer player;
    public final ServerBlockAccess blockAccess;
    public final ServerToolSet toolSet;
    public final BotSettings settings;
    public final Avoidance avoidance;

    // Precomputed values from settings
    public final boolean allowBreak;
    public final boolean allowPlace;
    public final boolean allowSprint;
    public final boolean allowParkour;
    public final int maxFallHeight;

    public CalculationContext(ServerLevel level, ServerPlayer player, BotSettings settings) {
        this(level, player, settings, Avoidance.empty());
    }

    public CalculationContext(ServerLevel level, ServerPlayer player, BotSettings settings, Avoidance avoidance) {
        this.level = level;
        this.player = player;
        this.blockAccess = new ServerBlockAccess(level);
        this.blockAccess.setSnapshotRadius(settings.snapshotRadius);
        this.toolSet = new ServerToolSet(player);
        this.settings = settings;
        this.avoidance = avoidance;

        this.allowBreak = settings.allowBreak;
        this.allowPlace = settings.allowPlace;
        this.allowSprint = settings.allowSprint;
        this.allowParkour = settings.allowParkour;
        this.maxFallHeight = settings.maxFallHeight;
    }

    /**
     * Thread-safe block state access. Reads from snapshot first,
     * returns STONE as fallback if position is outside snapshot.
     */
    public BlockState getBlockState(BlockPos pos) {
        BlockState state = blockAccess.getBlockState(pos);
        return state != null ? state : Blocks.STONE.defaultBlockState();
    }

    public BlockState getBlockState(int x, int y, int z) {
        BlockState state = blockAccess.getBlockState(x, y, z);
        return state != null ? state : Blocks.STONE.defaultBlockState();
    }
}
