package com.ultra.megamod.feature.backpacks.upgrade;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Interface for upgrades that need server-side ticking (smelting, magnet, feeding, etc.)
 */
public interface ITickableUpgrade {
    /**
     * Called every tick when the backpack is worn or placed as a block entity.
     */
    void tick(ServerPlayer player, ServerLevel level);

    /**
     * How often to tick (in game ticks). Return 1 for every tick, 5 for every 5th tick, etc.
     */
    default int getTickRate() { return 1; }
}
