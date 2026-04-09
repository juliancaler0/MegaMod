/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 */
package com.ultra.megamod.feature.dungeons.generation;

import java.util.List;
import net.minecraft.core.BlockPos;

public record RoomTemplate(RoomType type, BlockPos origin, int width, int height, int depth, List<BlockPos> doorways, List<BlockPos> spawnPoints, List<BlockPos> chestLocations) {
    public BlockPos center() {
        return this.origin.offset(this.width / 2, 1, this.depth / 2);
    }

    public BlockPos exitPoint() {
        return this.origin.offset(this.width / 2, 1, this.depth);
    }

    public static enum RoomType {
        ENTRANCE,
        CORRIDOR,
        SIDE_CORRIDOR,
        STAIRCASE,
        COMBAT,
        TREASURE,
        PUZZLE,
        BOSS,
        MINI_BOSS,
        TRAP_CORRIDOR,
        AMBUSH,        // Foliaath/Naga ambush room with tight corridors
        FLOODED,       // Water-filled room with underwater combat
        ARENA,         // Open circular arena with tiered seating
        GAUNTLET,      // Long narrow room with waves of enemies
        LIBRARY,       // Book-filled room with puzzle elements
        SNAKE,         // S-curve winding path with blind corners
        CHECKER,       // Checkerboard pillar grid for half-cover combat
        BRIDGE,        // Central bridge spanning a deep pit
        PLATFORM,      // Elevated platforms connected by walkways
        GRAND_HALL,    // Large open nave with columns
        PRISON,        // Iron-bar cells along walls with breakout mobs
        ZIGZAG,        // Alternating wall barriers forcing zigzag movement
        ORE_DEPOSIT,   // Walls/floor with exposed ore blocks
        CROSS,         // Cross-shaped room with 4 alcoves
        MAZE,          // Procedural maze with low ceiling
        VERTICAL,      // Two-story room with upper platform ring
        CORRIDOR_REWARD; // Small dead-end reward room

    }
}

