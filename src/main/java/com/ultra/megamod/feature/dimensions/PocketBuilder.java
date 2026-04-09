/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.ultra.megamod.feature.dimensions;

import com.ultra.megamod.feature.dimensions.DimensionRegistry;
import com.ultra.megamod.feature.dimensions.PortalBlock;
import com.ultra.megamod.feature.computer.ComputerRegistry;
import com.ultra.megamod.feature.marketplace.MarketplaceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;


public class PocketBuilder {
    private static final int MUSEUM_WIDTH = 21;
    private static final int MUSEUM_HEIGHT = 10;
    private static final int MUSEUM_DEPTH = 21;
    private static final int DUNGEON_WIDTH = 11;
    private static final int DUNGEON_HEIGHT = 7;
    private static final int DUNGEON_DEPTH = 11;

    private PocketBuilder() {
    }

    public static void buildMuseumShell(ServerLevel level, BlockPos origin) {
        BlockState wall = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState floor = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        BlockState ceiling = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState light = Blocks.GLOWSTONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        int maxX = 20;
        int maxY = 9;
        int maxZ = 20;
        for (int x = 0; x <= maxX; ++x) {
            for (int y = 0; y <= maxY; ++y) {
                for (int z = 0; z <= maxZ; ++z) {
                    boolean isEdgeZ;
                    BlockPos pos = origin.offset(x, y, z);
                    boolean isEdgeX = x == 0 || x == maxX;
                    boolean isEdgeY = y == 0 || y == maxY;
                    boolean bl = isEdgeZ = z == 0 || z == maxZ;
                    if (y == 0) {
                        level.setBlock(pos, floor, 3);
                        continue;
                    }
                    if (y == maxY) {
                        if (!isEdgeX && !isEdgeZ && x % 4 == 2 && z % 4 == 2) {
                            level.setBlock(pos, light, 3);
                            continue;
                        }
                        level.setBlock(pos, ceiling, 3);
                        continue;
                    }
                    if (isEdgeX || isEdgeZ) {
                        level.setBlock(pos, wall, 3);
                        continue;
                    }
                    level.setBlock(pos, air, 3);
                }
            }
        }
        /* Portal alcove on the left side of the north wall */
        BlockPos portalPos = origin.offset(3, 1, 1);
        PocketBuilder.placePortalBlock(level, portalPos);
        BlockState frame = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        // Frame around 1-wide x 2-tall portal: columns left/right + lintel
        level.setBlock(origin.offset(2, 1, 1), frame, 3);
        level.setBlock(origin.offset(4, 1, 1), frame, 3);
        level.setBlock(origin.offset(2, 2, 1), frame, 3);
        level.setBlock(origin.offset(4, 2, 1), frame, 3);
        level.setBlock(origin.offset(2, 3, 1), frame, 3);
        level.setBlock(origin.offset(3, 3, 1), frame, 3);
        level.setBlock(origin.offset(4, 3, 1), frame, 3);
    }

    /**
     * Builds a 25x25 basement room below the main hall for the achievement display.
     * Floor at origin.Y-6, ceiling at origin.Y-1, walls of polished blackstone.
     * Includes a staircase shaft connecting to the main hall floor level.
     */
    public static void buildMuseumBasement(ServerLevel level, BlockPos origin) {
        BlockState floor = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState ceiling = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState wall = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        BlockState light = Blocks.GLOWSTONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 25x25 room from origin.offset(-2, -6, -2) to origin.offset(22, -1, 22)
        int minX = -2;
        int maxX = 22;
        int minZ = -2;
        int maxZ = 22;
        int minY = -6;
        int maxY = -1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    boolean isEdgeX = (x == minX || x == maxX);
                    boolean isEdgeZ = (z == minZ || z == maxZ);
                    boolean isFloor = (y == minY);
                    boolean isCeiling = (y == maxY);

                    if (isFloor) {
                        level.setBlock(pos, floor, 3);
                    } else if (isCeiling) {
                        // Glowstone lights at regular intervals in the ceiling
                        if (!isEdgeX && !isEdgeZ && (x - minX) % 4 == 2 && (z - minZ) % 4 == 2) {
                            level.setBlock(pos, light, 3);
                        } else {
                            level.setBlock(pos, ceiling, 3);
                        }
                    } else if (isEdgeX || isEdgeZ) {
                        level.setBlock(pos, wall, 3);
                    } else {
                        level.setBlock(pos, air, 3);
                    }
                }
            }
        }

        // Staircase shaft and stairs are built dynamically by MuseumDisplayManager.buildBasementStaircase
    }

    public static void buildDungeonEntrance(ServerLevel level, BlockPos origin) {
        BlockState wall = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState floor = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        BlockState ceiling = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState light = Blocks.GLOWSTONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        int maxX = 10;
        int maxY = 6;
        int maxZ = 10;
        for (int x = 0; x <= maxX; ++x) {
            for (int y = 0; y <= maxY; ++y) {
                for (int z = 0; z <= maxZ; ++z) {
                    boolean isEdgeZ;
                    BlockPos pos = origin.offset(x, y, z);
                    boolean isEdgeX = x == 0 || x == maxX;
                    boolean isEdgeY = y == 0 || y == maxY;
                    boolean bl = isEdgeZ = z == 0 || z == maxZ;
                    if (y == 0) {
                        level.setBlock(pos, floor, 3);
                        continue;
                    }
                    if (y == maxY) {
                        if (!(isEdgeX || isEdgeZ || x != 2 && x != maxX - 2 || z != 2 && z != maxZ - 2)) {
                            level.setBlock(pos, light, 3);
                            continue;
                        }
                        level.setBlock(pos, ceiling, 3);
                        continue;
                    }
                    if (isEdgeX || isEdgeZ) {
                        level.setBlock(pos, wall, 3);
                        continue;
                    }
                    level.setBlock(pos, air, 3);
                }
            }
        }
        BlockPos portalPos = origin.offset(5, 1, 1);
        PocketBuilder.placePortalBlock(level, portalPos);
    }

    /**
     * Builds a 15x15x8 trading room with polished blackstone floor, glass walls,
     * glowstone lighting, two trading terminals facing each other, and a portal exit.
     */
    public static void buildTradingRoom(ServerLevel level, BlockPos origin) {
        BlockState floor = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        BlockState wall = Blocks.GLASS.defaultBlockState();
        BlockState ceiling = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        BlockState light = Blocks.GLOWSTONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState pillar = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();

        int maxX = 14;
        int maxY = 7;
        int maxZ = 14;

        for (int x = 0; x <= maxX; x++) {
            for (int y = 0; y <= maxY; y++) {
                for (int z = 0; z <= maxZ; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    boolean isEdgeX = (x == 0 || x == maxX);
                    boolean isEdgeZ = (z == 0 || z == maxZ);
                    boolean isCorner = isEdgeX && isEdgeZ;

                    if (y == 0) {
                        level.setBlock(pos, floor, 3);
                    } else if (y == maxY) {
                        // Ceiling with lights
                        if (!isEdgeX && !isEdgeZ && x % 4 == 2 && z % 4 == 2) {
                            level.setBlock(pos, light, 3);
                        } else {
                            level.setBlock(pos, ceiling, 3);
                        }
                    } else if (isCorner) {
                        // Pillars at corners
                        level.setBlock(pos, pillar, 3);
                    } else if (isEdgeX || isEdgeZ) {
                        // Glass walls
                        level.setBlock(pos, wall, 3);
                    } else {
                        level.setBlock(pos, air, 3);
                    }
                }
            }
        }

        // Place trading terminals facing each other at center
        // Terminal 1: at (5, 1, 7) facing east
        // Terminal 2: at (9, 1, 7) facing west
        BlockState terminalState = com.ultra.megamod.feature.marketplace.MarketplaceRegistry.TRADING_TERMINAL.get().defaultBlockState();
        level.setBlock(origin.offset(5, 1, 7), terminalState, 3);
        level.setBlock(origin.offset(9, 1, 7), terminalState, 3);

        // Portal exit at the north wall center
        BlockPos portalPos = origin.offset(7, 1, 1);
        PocketBuilder.placePortalBlock(level, portalPos);

        // Frame around portal
        BlockState frame = Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
        level.setBlock(origin.offset(6, 1, 1), frame, 3);
        level.setBlock(origin.offset(8, 1, 1), frame, 3);
        level.setBlock(origin.offset(6, 2, 1), frame, 3);
        level.setBlock(origin.offset(8, 2, 1), frame, 3);
        level.setBlock(origin.offset(6, 3, 1), frame, 3);
        level.setBlock(origin.offset(7, 3, 1), frame, 3);
        level.setBlock(origin.offset(8, 3, 1), frame, 3);

        // ATM on the east wall, facing west
        placeAtm(level, origin.offset(13, 1, 7), Direction.WEST);

        // Decorative center carpet
        BlockState carpet = Blocks.CYAN_CARPET.defaultBlockState();
        for (int x = 6; x <= 8; x++) {
            for (int z = 5; z <= 9; z++) {
                level.setBlock(origin.offset(x, 1, z), carpet, 3);
            }
        }
    }

    /**
     * Builds a 35x30x8 shared trading floor — a public marketplace area with stalls,
     * a central meeting area, and a portal exit.
     */
    public static void buildTradingFloor(ServerLevel level, BlockPos origin) {
        BlockState floor = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        BlockState floorAccent = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState wall = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        BlockState wallAccent = Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
        BlockState ceiling = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        BlockState light = Blocks.LANTERN.defaultBlockState();
        BlockState glow = Blocks.GLOWSTONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState stall = Blocks.BARREL.defaultBlockState();
        BlockState counter = Blocks.SMOOTH_STONE_SLAB.defaultBlockState();
        BlockState pillar = Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState();
        BlockState carpet = Blocks.ORANGE_CARPET.defaultBlockState();
        BlockState banner = Blocks.ORANGE_BANNER.defaultBlockState();

        int maxX = 34;
        int maxY = 7;
        int maxZ = 29;

        // Shell: floor, walls, ceiling
        for (int x = 0; x <= maxX; x++) {
            for (int y = 0; y <= maxY; y++) {
                for (int z = 0; z <= maxZ; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    boolean isEdgeX = (x == 0 || x == maxX);
                    boolean isEdgeZ = (z == 0 || z == maxZ);

                    if (y == 0) {
                        // Checkerboard floor
                        boolean accent = (x + z) % 4 == 0;
                        level.setBlock(pos, accent ? floorAccent : floor, 3);
                    } else if (y == maxY) {
                        // Ceiling with glow lights
                        if (!isEdgeX && !isEdgeZ && x % 5 == 2 && z % 5 == 2) {
                            level.setBlock(pos, glow, 3);
                        } else {
                            level.setBlock(pos, ceiling, 3);
                        }
                    } else if (isEdgeX && isEdgeZ) {
                        // Corner pillars
                        level.setBlock(pos, wallAccent, 3);
                    } else if (isEdgeX || isEdgeZ) {
                        // Walls with accent columns every 6 blocks
                        if ((isEdgeX && z % 6 == 0) || (isEdgeZ && x % 6 == 0)) {
                            level.setBlock(pos, wallAccent, 3);
                        } else {
                            level.setBlock(pos, wall, 3);
                        }
                    } else {
                        level.setBlock(pos, air, 3);
                    }
                }
            }
        }

        // Portal exit at north wall center
        BlockPos portalPos = origin.offset(17, 1, 1);
        PocketBuilder.placePortalBlock(level, portalPos);

        // Portal frame
        BlockState frame = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        level.setBlock(origin.offset(16, 1, 1), frame, 3);
        level.setBlock(origin.offset(18, 1, 1), frame, 3);
        level.setBlock(origin.offset(16, 2, 1), frame, 3);
        level.setBlock(origin.offset(18, 2, 1), frame, 3);
        level.setBlock(origin.offset(16, 3, 1), frame, 3);
        level.setBlock(origin.offset(17, 3, 1), frame, 3);
        level.setBlock(origin.offset(18, 3, 1), frame, 3);

        // Trading stalls — left side (x=3-13)
        for (int s = 0; s < 3; s++) {
            int sz = 6 + s * 8;
            // Counter
            for (int sx = 3; sx <= 7; sx++) {
                level.setBlock(origin.offset(sx, 1, sz), counter, 3);
            }
            // Back barrels
            for (int sx = 3; sx <= 7; sx++) {
                level.setBlock(origin.offset(sx, 1, sz + 1), stall, 3);
            }
            // Pillar at stall ends
            level.setBlock(origin.offset(3, 1, sz - 1), pillar, 3);
            level.setBlock(origin.offset(3, 2, sz - 1), pillar, 3);
            level.setBlock(origin.offset(7, 1, sz - 1), pillar, 3);
            level.setBlock(origin.offset(7, 2, sz - 1), pillar, 3);
        }

        // Trading stalls — right side (x=21-31)
        for (int s = 0; s < 3; s++) {
            int sz = 6 + s * 8;
            for (int sx = 27; sx <= 31; sx++) {
                level.setBlock(origin.offset(sx, 1, sz), counter, 3);
            }
            for (int sx = 27; sx <= 31; sx++) {
                level.setBlock(origin.offset(sx, 1, sz + 1), stall, 3);
            }
            level.setBlock(origin.offset(27, 1, sz - 1), pillar, 3);
            level.setBlock(origin.offset(27, 2, sz - 1), pillar, 3);
            level.setBlock(origin.offset(31, 1, sz - 1), pillar, 3);
            level.setBlock(origin.offset(31, 2, sz - 1), pillar, 3);
        }

        // Central meeting area — carpet path
        for (int z = 4; z <= 26; z++) {
            for (int x = 15; x <= 19; x++) {
                level.setBlock(origin.offset(x, 1, z), carpet, 3);
            }
        }

        // Central feature — trading table
        BlockState table = Blocks.CRAFTING_TABLE.defaultBlockState();
        level.setBlock(origin.offset(16, 1, 15), table, 3);
        level.setBlock(origin.offset(17, 1, 15), table, 3);
        level.setBlock(origin.offset(18, 1, 15), table, 3);

        // Lanterns on pillars along center
        for (int z = 8; z <= 24; z += 8) {
            level.setBlock(origin.offset(13, 1, z), wallAccent, 3);
            level.setBlock(origin.offset(13, 2, z), wallAccent, 3);
            level.setBlock(origin.offset(13, 3, z), light, 3);
            level.setBlock(origin.offset(21, 1, z), wallAccent, 3);
            level.setBlock(origin.offset(21, 2, z), wallAccent, 3);
            level.setBlock(origin.offset(21, 3, z), light, 3);
        }

        // Banners at entrance
        level.setBlock(origin.offset(15, 3, 2), banner, 3);
        level.setBlock(origin.offset(19, 3, 2), banner, 3);

        // ATMs along the back (south) wall — two ATMs spaced out
        placeAtm(level, origin.offset(10, 1, 28), Direction.NORTH);
        placeAtm(level, origin.offset(24, 1, 28), Direction.NORTH);

        // Trading terminals at each stall section — left side
        BlockState terminalState = MarketplaceRegistry.TRADING_TERMINAL.get().defaultBlockState();
        level.setBlock(origin.offset(5, 1, 5), terminalState, 3);
        level.setBlock(origin.offset(5, 1, 13), terminalState, 3);
        level.setBlock(origin.offset(5, 1, 21), terminalState, 3);

        // Trading terminals — right side
        level.setBlock(origin.offset(29, 1, 5), terminalState, 3);
        level.setBlock(origin.offset(29, 1, 13), terminalState, 3);
        level.setBlock(origin.offset(29, 1, 21), terminalState, 3);
    }

    /**
     * Places a 2-tall ATM block facing the given direction.
     */
    private static void placeAtm(ServerLevel level, BlockPos pos, Direction facing) {
        BlockState lower = ComputerRegistry.ATM_BLOCK.get().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        BlockState upper = lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
        level.setBlock(pos, lower, 3);
        level.setBlock(pos.above(), upper, 3);
    }

    public static void placePortalBlock(ServerLevel level, BlockPos pos) {
        BlockState portalState = ((PortalBlock)((Object)DimensionRegistry.PORTAL_BLOCK.get())).defaultBlockState();
        // Place a 1x1x2 portal (1 wide, 2 tall) — player-sized doorway
        level.setBlock(pos, portalState, 3);
        level.setBlock(pos.above(), portalState, 3);
    }
}

