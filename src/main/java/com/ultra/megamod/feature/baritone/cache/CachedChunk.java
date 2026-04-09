package com.ultra.megamod.feature.baritone.cache;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.Fluids;

/**
 * Stores a snapshot of block type classifications for a single chunk column.
 * Uses a compact byte array where each byte represents a block type category:
 * 0=air, 1=solid, 2=water, 3=lava, 4=dangerous, 5=climbable.
 * Much faster than querying level.getBlockState() during pathfinding.
 */
public class CachedChunk {
    /** Block type: air or passable non-solid */
    public static final byte AIR = 0;
    /** Block type: solid, walkable surface */
    public static final byte SOLID = 1;
    /** Block type: water */
    public static final byte WATER = 2;
    /** Block type: lava */
    public static final byte LAVA = 3;
    /** Block type: dangerous (fire, cactus, magma, sweet berry, wither rose, etc.) */
    public static final byte DANGEROUS = 4;
    /** Block type: climbable (ladder, vine, scaffolding) */
    public static final byte CLIMBABLE = 5;

    private final int chunkX;
    private final int chunkZ;
    private final int minY;
    private final int height; // Total Y range (e.g. 384 for overworld -64 to 320)
    private final byte[][][] blocks; // [16][height][16] indexed by localX, adjustedY, localZ
    private long lastUpdated;

    public CachedChunk(int chunkX, int chunkZ, int minY, int height) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.minY = minY;
        this.height = height;
        this.blocks = new byte[16][height][16];
        this.lastUpdated = 0;
    }

    /**
     * Take a snapshot of all blocks in this chunk from the level.
     * Should be called on the server thread.
     */
    public void snapshot(ServerLevel level) {
        if (!level.hasChunk(chunkX, chunkZ)) return;

        LevelChunk chunk = level.getChunk(chunkX, chunkZ);
        LevelChunkSection[] sections = chunk.getSections();
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;

        for (int sectionIdx = 0; sectionIdx < sections.length; sectionIdx++) {
            LevelChunkSection section = sections[sectionIdx];
            int sectionBaseY = minY + (sectionIdx << 4);

            if (section == null || section.hasOnlyAir()) {
                // Fill section with AIR
                for (int lx = 0; lx < 16; lx++) {
                    for (int ly = 0; ly < 16; ly++) {
                        int adjustedY = sectionBaseY - minY + ly;
                        if (adjustedY >= 0 && adjustedY < height) {
                            for (int lz = 0; lz < 16; lz++) {
                                blocks[lx][adjustedY][lz] = AIR;
                            }
                        }
                    }
                }
                continue;
            }

            for (int lx = 0; lx < 16; lx++) {
                for (int ly = 0; ly < 16; ly++) {
                    int adjustedY = sectionBaseY - minY + ly;
                    if (adjustedY < 0 || adjustedY >= height) continue;

                    for (int lz = 0; lz < 16; lz++) {
                        BlockState state = section.getBlockState(lx, ly, lz);
                        blocks[lx][adjustedY][lz] = classify(state);
                    }
                }
            }
        }
        lastUpdated = System.currentTimeMillis();
    }

    /**
     * Invalidate a single block position (mark as needing re-read on next snapshot).
     * For immediate updates, directly reclassify from a known state.
     */
    public void setBlockType(int localX, int y, int localZ, byte type) {
        int adjustedY = y - minY;
        if (localX >= 0 && localX < 16 && adjustedY >= 0 && adjustedY < height && localZ >= 0 && localZ < 16) {
            blocks[localX][adjustedY][localZ] = type;
        }
    }

    /**
     * Get the cached block type at a local position.
     *
     * @param localX 0-15 within chunk
     * @param y      World Y coordinate
     * @param localZ 0-15 within chunk
     * @return Block type constant (AIR, SOLID, WATER, LAVA, DANGEROUS, CLIMBABLE)
     */
    public byte getBlockType(int localX, int y, int localZ) {
        int adjustedY = y - minY;
        if (localX < 0 || localX >= 16 || adjustedY < 0 || adjustedY >= height || localZ < 0 || localZ >= 16) {
            return SOLID; // Out of bounds = treat as solid (safe default)
        }
        return blocks[localX][adjustedY][localZ];
    }

    public boolean isSolid(int localX, int y, int localZ) {
        return getBlockType(localX, y, localZ) == SOLID;
    }

    public boolean isAir(int localX, int y, int localZ) {
        return getBlockType(localX, y, localZ) == AIR;
    }

    public boolean isWater(int localX, int y, int localZ) {
        return getBlockType(localX, y, localZ) == WATER;
    }

    public boolean isLava(int localX, int y, int localZ) {
        return getBlockType(localX, y, localZ) == LAVA;
    }

    public boolean isDangerous(int localX, int y, int localZ) {
        byte type = getBlockType(localX, y, localZ);
        return type == DANGEROUS || type == LAVA;
    }

    public boolean isClimbable(int localX, int y, int localZ) {
        return getBlockType(localX, y, localZ) == CLIMBABLE;
    }

    public boolean isPassable(int localX, int y, int localZ) {
        byte type = getBlockType(localX, y, localZ);
        return type == AIR || type == WATER || type == CLIMBABLE;
    }

    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public long getLastUpdated() { return lastUpdated; }

    /**
     * Age in milliseconds since last snapshot.
     */
    public long getAge() {
        return System.currentTimeMillis() - lastUpdated;
    }

    /**
     * Whether the snapshot has never been taken.
     */
    public boolean isEmpty() {
        return lastUpdated == 0;
    }

    /**
     * Classify a block state into one of the cached type categories.
     */
    private static byte classify(BlockState state) {
        if (state.isAir()) return AIR;

        // Check fluids
        if (!state.getFluidState().isEmpty()) {
            if (state.getFluidState().is(Fluids.LAVA) || state.getFluidState().is(Fluids.FLOWING_LAVA)) {
                return LAVA;
            }
            if (state.getFluidState().is(Fluids.WATER) || state.getFluidState().is(Fluids.FLOWING_WATER)) {
                return WATER;
            }
        }

        // Dangerous blocks
        if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)
            || state.is(Blocks.CACTUS) || state.is(Blocks.MAGMA_BLOCK)
            || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.WITHER_ROSE)
            || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.CAMPFIRE)
            || state.is(Blocks.SOUL_CAMPFIRE)) {
            return DANGEROUS;
        }

        // Climbable blocks
        if (state.is(BlockTags.CLIMBABLE)) {
            return CLIMBABLE;
        }

        // Non-solid passable blocks (doors, trapdoors, signs, flowers, grass, etc.)
        if (!state.isSolid()) {
            return AIR;
        }

        return SOLID;
    }
}
