package com.ultra.megamod.feature.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Builds arena rooms in the pocket dimension.
 * PvE arena is a Roman colosseum with tiered seating, pillars, and sand floor.
 */
public class ArenaBuilder {

    // Floor materials
    private static final BlockState SAND = Blocks.RED_SAND.defaultBlockState();
    private static final BlockState SANDSTONE = Blocks.SMOOTH_SANDSTONE.defaultBlockState();
    private static final BlockState CUT_SANDSTONE = Blocks.CUT_SANDSTONE.defaultBlockState();
    private static final BlockState CHISELED_SANDSTONE = Blocks.CHISELED_SANDSTONE.defaultBlockState();

    // Structure materials
    private static final BlockState STONE_BRICK = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState STONE_BRICK_SLAB = Blocks.STONE_BRICK_SLAB.defaultBlockState();
    private static final BlockState STONE_BRICK_STAIRS = Blocks.STONE_BRICK_STAIRS.defaultBlockState();
    private static final BlockState POLISHED_DEEPSLATE = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
    private static final BlockState DEEPSLATE_BRICKS = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
    private static final BlockState DEEPSLATE_TILES = Blocks.DEEPSLATE_TILES.defaultBlockState();

    // Decorative
    private static final BlockState QUARTZ_PILLAR = Blocks.QUARTZ_PILLAR.defaultBlockState();
    private static final BlockState QUARTZ_BLOCK = Blocks.QUARTZ_BLOCK.defaultBlockState();
    private static final BlockState LANTERN = Blocks.LANTERN.defaultBlockState();
    private static final BlockState SOUL_LANTERN = Blocks.SOUL_LANTERN.defaultBlockState();
    private static final BlockState IRON_BARS = Blocks.IRON_BARS.defaultBlockState();
    private static final BlockState GOLD_BLOCK = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState REDSTONE_BLOCK = Blocks.REDSTONE_BLOCK.defaultBlockState();

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState BARRIER = Blocks.BARRIER.defaultBlockState();
    private static final BlockState GLOWSTONE = Blocks.GLOWSTONE.defaultBlockState();

    private static final int ARENA_RADIUS = 20;    // fighting pit radius
    private static final int SEATING_ROWS = 5;     // rows of seating
    private static final int WALL_HEIGHT = 10;      // total wall height
    private static final int SIZE = ARENA_RADIUS * 2 + SEATING_ROWS * 2 + 3; // full footprint
    private static final int CENTER = SIZE / 2;
    private static final int PILLAR_COUNT = 16;     // pillars around upper rim

    /**
     * Build a colosseum-style PvE arena.
     * Circular fighting pit with sand floor, tiered stone seating,
     * quartz pillars on top, lanterns, and decorative details.
     */
    public static void buildPveArena(ServerLevel level, BlockPos origin) {
        int totalR = ARENA_RADIUS + SEATING_ROWS + 1;

        // Pass 1: Clear air + place solid foundation at y=-1 and y=0
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                // Solid foundation so nothing falls into void
                level.setBlock(origin.offset(x, -1, z), STONE_BRICK, 2);
                level.setBlock(origin.offset(x, 0, z), STONE_BRICK, 2);
                // Clear above
                for (int y = 1; y <= WALL_HEIGHT + 3; y++) {
                    level.setBlock(origin.offset(x, y, z), AIR, 2);
                }
            }
        }

        // Pass 2: Build the colosseum
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int dx = x - CENTER;
                int dz = z - CENTER;
                double dist = Math.sqrt(dx * dx + dz * dz);

                if (dist <= ARENA_RADIUS) {
                    // === FIGHTING PIT FLOOR ===
                    level.setBlock(origin.offset(x, 0, z), SAND, 2);

                    // Decorative ring near the edge
                    if (dist >= ARENA_RADIUS - 2 && dist <= ARENA_RADIUS) {
                        level.setBlock(origin.offset(x, 0, z), CUT_SANDSTONE, 2);
                    }
                    // Center cross pattern
                    if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                        level.setBlock(origin.offset(x, 0, z), CHISELED_SANDSTONE, 2);
                    }

                } else if (dist <= ARENA_RADIUS + 1) {
                    // === ARENA WALL (pit edge) ===
                    level.setBlock(origin.offset(x, 0, z), DEEPSLATE_BRICKS, 2);
                    level.setBlock(origin.offset(x, 1, z), DEEPSLATE_BRICKS, 2);
                    level.setBlock(origin.offset(x, 2, z), POLISHED_DEEPSLATE, 2);
                    // Iron bars on top of wall
                    level.setBlock(origin.offset(x, 3, z), IRON_BARS, 2);

                } else if (dist <= totalR) {
                    // === TIERED SEATING ===
                    int seatRow = (int)(dist - ARENA_RADIUS - 1);
                    seatRow = Math.min(seatRow, SEATING_ROWS - 1);

                    // Foundation
                    for (int y = 0; y <= seatRow + 2; y++) {
                        level.setBlock(origin.offset(x, y, z), STONE_BRICK, 2);
                    }
                    // Seat surface
                    level.setBlock(origin.offset(x, seatRow + 3, z), SANDSTONE, 2);

                } else if (dist <= totalR + 1) {
                    // === OUTER WALL ===
                    for (int y = 0; y <= WALL_HEIGHT; y++) {
                        level.setBlock(origin.offset(x, y, z), DEEPSLATE_TILES, 2);
                    }
                }
            }
        }

        // Pass 3: Pillars around the top rim
        for (int i = 0; i < PILLAR_COUNT; i++) {
            double angle = Math.PI * 2 * i / PILLAR_COUNT;
            int px = CENTER + (int)(Math.cos(angle) * (totalR + 0.5));
            int pz = CENTER + (int)(Math.sin(angle) * (totalR + 0.5));

            // Quartz pillar (6 blocks tall from seating top)
            int pillarBase = SEATING_ROWS + 3;
            for (int y = pillarBase; y <= pillarBase + 5; y++) {
                level.setBlock(origin.offset(px, y, pz), QUARTZ_PILLAR, 2);
            }
            // Capital (quartz block on top)
            level.setBlock(origin.offset(px, pillarBase + 6, pz), QUARTZ_BLOCK, 2);

            // Lantern on every other pillar
            if (i % 2 == 0) {
                level.setBlock(origin.offset(px, pillarBase + 5, pz + 1), LANTERN, 2);
                level.setBlock(origin.offset(px, pillarBase + 5, pz - 1), LANTERN, 2);
            } else {
                level.setBlock(origin.offset(px + 1, pillarBase + 5, pz), SOUL_LANTERN, 2);
                level.setBlock(origin.offset(px - 1, pillarBase + 5, pz), SOUL_LANTERN, 2);
            }
        }

        // Pass 4: Lighting — ring of glowstone under seating (hidden)
        for (int i = 0; i < 24; i++) {
            double angle = Math.PI * 2 * i / 24;
            int lx = CENTER + (int)(Math.cos(angle) * (ARENA_RADIUS + 2));
            int lz = CENTER + (int)(Math.sin(angle) * (ARENA_RADIUS + 2));
            level.setBlock(origin.offset(lx, 1, lz), GLOWSTONE, 2);
        }
        // Sky light: glowstone ring above pillars
        for (int i = 0; i < 32; i++) {
            double angle = Math.PI * 2 * i / 32;
            int lx = CENTER + (int)(Math.cos(angle) * (ARENA_RADIUS - 3));
            int lz = CENTER + (int)(Math.sin(angle) * (ARENA_RADIUS - 3));
            level.setBlock(origin.offset(lx, WALL_HEIGHT + 2, lz), GLOWSTONE, 2);
        }

        // Pass 5: Player spawn pad (center, gold)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(origin.offset(CENTER + dx, 0, CENTER + dz), GOLD_BLOCK, 2);
            }
        }

        // Pass 6: Mob spawn pads (4 corners of the pit, redstone under sand)
        int[][] mobSpawns = {{-12, -12}, {-12, 12}, {12, -12}, {12, 12}};
        for (int[] ms : mobSpawns) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    level.setBlock(origin.offset(CENTER + ms[0] + dx, 0, CENTER + ms[1] + dz), REDSTONE_BLOCK, 2);
                }
            }
        }

        // Barrier ceiling (prevents escape)
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int dx2 = x - CENTER;
                int dz2 = z - CENTER;
                double d2 = Math.sqrt(dx2 * dx2 + dz2 * dz2);
                if (d2 <= totalR + 2) {
                    level.setBlock(origin.offset(x, WALL_HEIGHT + 3, z), BARRIER, 2);
                }
            }
        }
    }

    /**
     * Build a 21x21 PvP arena.
     * Stone brick floor, barrier walls, 2 spawn pads on opposite sides.
     */
    public static void buildPvpArena(ServerLevel level, BlockPos origin) {
        int sizeX = 21;
        int sizeZ = 21;
        int wallHeight = 5;

        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                BlockPos floorPos = origin.offset(x, 0, z);
                level.setBlock(floorPos, STONE_BRICK, 2);

                for (int y = 1; y <= wallHeight; y++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (x == 0 || x == sizeX - 1 || z == 0 || z == sizeZ - 1) {
                        level.setBlock(pos, BARRIER, 2);
                    } else {
                        level.setBlock(pos, AIR, 2);
                    }
                }

                BlockPos ceilPos = origin.offset(x, wallHeight + 1, z);
                level.setBlock(ceilPos, GLOWSTONE, 2);
            }
        }

        // Spawn pads
        int midX = sizeX / 2;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                level.setBlock(origin.offset(midX + dx, 0, 2 + dz), GOLD_BLOCK, 2);
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                level.setBlock(origin.offset(midX + dx, 0, sizeZ - 3 + dz), GOLD_BLOCK, 2);
            }
        }
    }
}
