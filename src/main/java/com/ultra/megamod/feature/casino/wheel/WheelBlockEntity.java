package com.ultra.megamod.feature.casino.wheel;

import com.ultra.megamod.feature.casino.CasinoRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WheelBlockEntity extends BlockEntity {
    private WheelGame game;

    // Client-side rendering state (updated by WheelSyncPayload handler)
    public static volatile float clientSpinAngle = 0f;
    public static volatile String clientPhase = "BETTING";
    public static volatile int clientResult = -1;
    public static volatile int clientTimer = 0;

    public WheelBlockEntity(BlockPos pos, BlockState state) {
        super(CasinoRegistry.WHEEL_BE.get(), pos, state);
    }

    /**
     * Lazy initializes and returns the WheelGame instance.
     * The game is transient and not persisted to disk.
     */
    public WheelGame getOrCreateGame() {
        if (game == null) {
            game = new WheelGame();
        }
        return game;
    }

    /**
     * Returns the current WheelGame instance, or null if not yet created.
     */
    public WheelGame getGame() {
        return game;
    }

    /**
     * Static tick method called by the block entity ticker from WheelBlock.
     * Only ticks on the server side.
     */
    public static void tick(Level level, BlockPos pos, BlockState state, WheelBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        blockEntity.getOrCreateGame().tick(serverLevel);
    }
}
