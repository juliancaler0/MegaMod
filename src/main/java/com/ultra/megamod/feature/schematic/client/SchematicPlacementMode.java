package com.ultra.megamod.feature.schematic.client;

import com.ultra.megamod.feature.schematic.data.SchematicData;
import com.ultra.megamod.feature.schematic.placement.SchematicPlacement;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

/**
 * Client-side state machine for schematic placement mode.
 * Tracks the active placement and cached world blocks.
 */
public class SchematicPlacementMode {

    public enum State { NONE, PLACING, ADJUSTING }

    private static State currentState = State.NONE;
    private static SchematicPlacement activePlacement = null;
    private static Map<BlockPos, BlockState> cachedWorldBlocks = Collections.emptyMap();
    private static boolean blocksCacheDirty = true;

    public static State getState() { return currentState; }
    public static boolean isActive() { return currentState != State.NONE; }

    @Nullable
    public static SchematicPlacement getActivePlacement() { return activePlacement; }

    /**
     * Enters placement mode with a loaded schematic at the player's feet.
     */
    public static void startPlacement(SchematicData schematic) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        activePlacement = new SchematicPlacement(schematic, playerPos);
        currentState = State.PLACING;
        blocksCacheDirty = true;
    }

    /**
     * Enters placement mode with an existing placement.
     */
    public static void startPlacement(SchematicPlacement placement) {
        activePlacement = placement;
        currentState = State.PLACING;
        blocksCacheDirty = true;
    }

    /**
     * Cancels placement mode.
     */
    public static void cancel() {
        activePlacement = null;
        cachedWorldBlocks = Collections.emptyMap();
        currentState = State.NONE;
        blocksCacheDirty = true;
    }

    /**
     * Marks the placement as accepted (locked) and transitions to ADJUSTING state.
     * The caller should send the placement to the server.
     */
    public static void accept() {
        if (activePlacement != null) {
            activePlacement.setLocked(true);
        }
        currentState = State.ADJUSTING;
    }

    /**
     * Clears placement mode entirely after server confirms.
     */
    public static void clear() {
        activePlacement = null;
        cachedWorldBlocks = Collections.emptyMap();
        currentState = State.NONE;
        blocksCacheDirty = true;
    }

    /**
     * Marks the block cache as dirty (needs recalculation).
     * Call this whenever the placement origin, rotation, or mirror changes.
     */
    public static void markDirty() {
        blocksCacheDirty = true;
    }

    /**
     * Returns the cached world blocks map, recalculating if dirty.
     */
    public static Map<BlockPos, BlockState> getWorldBlocks() {
        if (blocksCacheDirty && activePlacement != null) {
            cachedWorldBlocks = activePlacement.getWorldBlocks();
            blocksCacheDirty = false;
        }
        return cachedWorldBlocks;
    }
}
