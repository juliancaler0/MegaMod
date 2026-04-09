package com.ultra.megamod.feature.computer.screen.map;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.client.renderer.BiomeColors;
// FoliageColor constants for birch/spruce (fixed, not biome-dependent)
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.material.MapColor;
import java.util.Set;

/**
 * Renders one 16x16 chunk into a NativeImage using MC's MapColor system.
 * Ported from recruits ChunkImage.java, adapted for MegaMod.
 *
 * Uses NativeImage.setPixel/getPixel which work in ARGB format,
 * and MapColor.calculateARGBColor which also returns ARGB.
 */
public class MapChunkImage {
    private final NativeImage image;

    // Fixed foliage colors for birch and spruce (not biome-dependent)
    private static final int BIRCH_FOLIAGE_COLOR = 0x80A755;
    private static final int SPRUCE_FOLIAGE_COLOR = 0x619961;

    // Blocks that should use biome grass tint instead of flat MapColor
    private static final Set<Block> GRASS_TINTED = Set.of(
        Blocks.GRASS_BLOCK, Blocks.SHORT_GRASS, Blocks.TALL_GRASS,
        Blocks.FERN, Blocks.LARGE_FERN
    );

    public MapChunkImage(ClientLevel level, ChunkPos pos, boolean caveView) {
        this.image = generateImage(level, pos, caveView);
    }

    /**
     * Convert ARGB (0xAARRGGBB) to ABGR (0xAABBGGRR) for NativeImage.setPixel.
     * NativeImage Format.RGBA stores bytes as R,G,B,A in memory; on little-endian
     * systems, reading as an int yields ABGR. MapColor and BiomeColors return ARGB.
     */
    private static int argbToAbgr(int argb) {
        return (argb & 0xFF00FF00) | ((argb >> 16) & 0xFF) | ((argb & 0xFF) << 16);
    }

    private NativeImage generateImage(ClientLevel level, ChunkPos pos, boolean caveView) {
        NativeImage img = new NativeImage(NativeImage.Format.RGBA, 16, 16, true);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = pos.getMinBlockX() + x;
                int worldZ = pos.getMinBlockZ() + z;
                int worldY;
                if (caveView) {
                    worldY = 30;
                } else {
                    worldY = level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ) - 1;
                }
                img.setPixel(x, z, argbToAbgr(getReliefColor(level, new BlockPos(worldX, worldY, worldZ), caveView)));
            }
        }
        img.untrack();
        return img;
    }

    private int getReliefColor(ClientLevel level, BlockPos pos, boolean caveView) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            if (caveView) {
                // In cave view, search downward for a non-air block
                BlockPos.MutableBlockPos mutable = pos.mutable();
                for (int i = 0; i < 30; i++) {
                    mutable.move(Direction.DOWN);
                    state = level.getBlockState(mutable);
                    if (!state.isAir()) {
                        pos = mutable.immutable();
                        break;
                    }
                }
                if (state.isAir()) return 0xFF000000;
            } else {
                return 0xFF000000;
            }
        }

        boolean isWaterLike = state.getFluidState().is(Fluids.WATER);

        if (isWaterLike) {
            int depth = getWaterDepth(level, pos);
            MapColor.Brightness brightness =
                    depth > 6 ? MapColor.Brightness.LOWEST :
                            depth > 3 ? MapColor.Brightness.LOW :
                                    MapColor.Brightness.NORMAL;
            // Use biome-specific water color (swamp=murky, warm ocean=light blue, etc.)
            try {
                int biomeWaterColor = BiomeColors.getAverageWaterColor(level, pos);
                return applyBrightness(biomeWaterColor, brightness);
            } catch (Exception e) {
                BlockState topState = getTopWaterBlock(level, pos);
                MapColor mapColor = topState.getMapColor(level, pos);
                if (mapColor == null) mapColor = MapColor.WATER;
                return mapColor.calculateARGBColor(brightness);
            }
        }

        // Compute relief brightness
        MapColor.Brightness brightness;
        if (caveView) {
            brightness = MapColor.Brightness.NORMAL;
        } else {
            int heightHere = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
            int heightSouth = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ() + 1);
            int heightWest = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX() - 1, pos.getZ());
            int relHeight = heightHere - Math.max(heightSouth, heightWest);
            brightness =
                    relHeight > 2 ? MapColor.Brightness.HIGH :
                            relHeight > 0 ? MapColor.Brightness.NORMAL :
                                    relHeight > -2 ? MapColor.Brightness.LOW :
                                            MapColor.Brightness.LOWEST;
        }

        // Biome grass tinting — plains vs jungle vs swamp grass all look different
        Block block = state.getBlock();
        if (GRASS_TINTED.contains(block)) {
            try {
                int grassColor = BiomeColors.getAverageGrassColor(level, pos);
                return applyBrightness(grassColor, brightness);
            } catch (Exception ignored) {}
        }

        // Biome foliage tinting (leaves, vines)
        if (block instanceof LeavesBlock) {
            try {
                int foliageColor;
                if (block == Blocks.BIRCH_LEAVES) {
                    foliageColor = BIRCH_FOLIAGE_COLOR;
                } else if (block == Blocks.SPRUCE_LEAVES) {
                    foliageColor = SPRUCE_FOLIAGE_COLOR;
                } else {
                    foliageColor = BiomeColors.getAverageFoliageColor(level, pos);
                }
                return applyBrightness(foliageColor, brightness);
            } catch (Exception ignored) {}
        }
        if (block == Blocks.VINE) {
            try {
                return applyBrightness(BiomeColors.getAverageFoliageColor(level, pos), brightness);
            } catch (Exception ignored) {}
        }

        // Default: use MapColor system
        MapColor mapColor = state.getMapColor(level, pos);
        if (mapColor == null) return 0xFF000000;
        return mapColor.calculateARGBColor(brightness);
    }

    /**
     * Applies a brightness multiplier to an RGB color, matching MapColor's brightness system.
     */
    private int applyBrightness(int color, MapColor.Brightness brightness) {
        int mult = switch (brightness) {
            case HIGH -> 255;
            case NORMAL -> 220;
            case LOW -> 180;
            case LOWEST -> 135;
        };
        int r = ((color >> 16) & 0xFF) * mult / 255;
        int g = ((color >> 8) & 0xFF) * mult / 255;
        int b = (color & 0xFF) * mult / 255;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private BlockState getTopWaterBlock(ClientLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        while (isWaterLike(level.getBlockState(mutable))
                && mutable.getY() < level.getMaxY()) {
            mutable.move(Direction.UP);
        }
        return level.getBlockState(mutable.below());
    }

    private int getWaterDepth(ClientLevel level, BlockPos pos) {
        int depth = 0;
        BlockPos.MutableBlockPos mutable = pos.mutable();
        while (isWaterLike(level.getBlockState(mutable))
                && mutable.getY() > level.getMinY()) {
            depth++;
            mutable.move(Direction.DOWN);
        }
        return depth;
    }

    private boolean isWaterLike(BlockState state) {
        return state.getFluidState().is(Fluids.WATER);
    }

    public NativeImage getNativeImage() {
        return this.image;
    }

    /**
     * Returns true if this chunk image has enough meaningful (non-black, non-transparent) pixels.
     * Avoids merging empty/void chunks into tiles.
     */
    public boolean isMeaningful() {
        if (this.image == null) return false;
        int meaningful = 0;
        for (int i = 0; i < 256; i++) {
            int pixel = this.image.getPixel(i % 16, i / 16);
            int alpha = (pixel >> 24) & 0xFF;
            int rgb = pixel & 0x00FFFFFF;
            if (alpha > 0 && rgb != 0) meaningful++;
        }
        return meaningful >= 25; // ~10% of 256
    }

    public void close() {
        try {
            if (image != null) image.close();
        } catch (Exception ignored) {}
    }
}
