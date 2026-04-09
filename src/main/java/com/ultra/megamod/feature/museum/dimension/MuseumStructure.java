package com.ultra.megamod.feature.museum.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Builds corridor stubs from the main hall to each wing.
 * Corridors are 3 wide × 4 deep × 3 tall and are built once on first visit.
 * The actual wing hallways are built dynamically by MuseumDisplayManager each visit.
 */
public class MuseumStructure {
    private static final int CORRIDOR_WIDTH = 3;
    private static final int CORRIDOR_HEIGHT = 3;
    private static final int CORRIDOR_DEPTH = 4;

    private MuseumStructure() {
    }

    /**
     * Build just the corridor stubs from main hall walls to wing entrances.
     * Called once on first museum visit. Wings are built dynamically by MuseumDisplayManager.
     */
    public static void buildCorridorStubs(ServerLevel level, BlockPos origin) {
        int centerX = origin.getX() + 10;
        int floorY = origin.getY();
        int centerZ = origin.getZ() + 10;

        // South corridor (Items Wing) — from south wall of main hall
        BlockPos southStart = new BlockPos(centerX - 1, floorY, origin.getZ() + 21 - 1);
        buildCorridor(level, southStart, Direction.SOUTH, "Items Wing");

        // North corridor (Art Wing) — from north wall of main hall
        BlockPos northStart = new BlockPos(centerX - 1, floorY, origin.getZ());
        buildCorridor(level, northStart, Direction.NORTH, "Art Wing");

        // East corridor (Aquarium Wing) — from east wall of main hall
        BlockPos eastStart = new BlockPos(origin.getX() + 21 - 1, floorY, centerZ - 1);
        buildCorridor(level, eastStart, Direction.EAST, "Aquarium Wing");

        // West corridor (Wildlife Wing) — from west wall of main hall
        BlockPos westStart = new BlockPos(origin.getX(), floorY, centerZ - 1);
        buildCorridor(level, westStart, Direction.WEST, "Wildlife Wing");

        // Achievements displayed in main hall — no separate wing needed
    }

    /**
     * Build a single corridor stub: punch hole in main hall wall, then build 4-deep tunnel.
     *
     * @param wallPos   The wall position (bottom-left of the 3-wide hole)
     * @param outward   Direction going away from main hall into the wing
     * @param name      Wing name (unused here, kept for consistency)
     */
    private static void buildCorridor(ServerLevel level, BlockPos wallPos, Direction outward, String name) {
        int dx = outward.getStepX();
        int dz = outward.getStepZ();
        // Perpendicular axis
        int px = (dx != 0) ? 0 : 1;
        int pz = (dx != 0) ? 1 : 0;

        // Punch hole in main hall wall (3 wide × 3 tall)
        for (int w = 0; w < CORRIDOR_WIDTH; w++) {
            for (int h = 1; h <= CORRIDOR_HEIGHT; h++) {
                BlockPos holePos = wallPos.offset(w * px, h, w * pz);
                level.setBlock(holePos, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        // Build corridor tunnel (4 blocks deep, 5 wide including walls, 5 tall including floor/ceiling)
        BlockState wall = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState floor = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int d = 1; d <= CORRIDOR_DEPTH; d++) {
            for (int w = -1; w <= CORRIDOR_WIDTH; w++) {
                for (int h = 0; h <= CORRIDOR_HEIGHT + 1; h++) {
                    BlockPos pos = wallPos.offset(d * dx + w * px, h, d * dz + w * pz);
                    boolean isWall = (w == -1 || w == CORRIDOR_WIDTH);
                    boolean isFloor = (h == 0);
                    boolean isCeiling = (h == CORRIDOR_HEIGHT + 1);

                    if (isFloor) {
                        level.setBlock(pos, floor, 3);
                    } else if (isCeiling) {
                        level.setBlock(pos, wall, 3);
                    } else if (isWall) {
                        level.setBlock(pos, wall, 3);
                    } else {
                        level.setBlock(pos, air, 3);
                    }
                }
            }
        }
    }

    /**
     * Get the position where the corridor ends (1 block past the last corridor block).
     * This is where the wing hallway starts.
     *
     * @param wallPos  Same wallPos used in buildCorridor
     * @param outward  Direction from main hall to wing
     * @return BlockPos at corridor exit (floor level)
     */
    public static BlockPos getCorridorEnd(BlockPos wallPos, Direction outward) {
        int dx = outward.getStepX();
        int dz = outward.getStepZ();
        return wallPos.offset((CORRIDOR_DEPTH + 1) * dx, 0, (CORRIDOR_DEPTH + 1) * dz);
    }
}
