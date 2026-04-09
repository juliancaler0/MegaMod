package com.ultra.megamod.feature.citizen.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;

/**
 * StructureProcessor that handles placeholder block replacement in world-gen colonies.
 * <ul>
 *   <li>Structure void blocks are replaced with air.</li>
 *   <li>Light blue wool (substitution marker) is replaced with biome-appropriate terrain.</li>
 *   <li>Magenta wool (path marker) is replaced with a dirt path block.</li>
 * </ul>
 */
public class ColonyStructureProcessor extends StructureProcessor {

    public static final ColonyStructureProcessor INSTANCE = new ColonyStructureProcessor();
    public static final MapCodec<ColonyStructureProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    private ColonyStructureProcessor() {}

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level,
            BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo,
            StructureTemplate.StructureBlockInfo relativeBlockInfo,
            StructurePlaceSettings settings) {

        BlockState state = relativeBlockInfo.state();
        Block block = state.getBlock();

        // Structure void -> air (let the world fill in naturally)
        if (block == Blocks.STRUCTURE_VOID) {
            return new StructureTemplate.StructureBlockInfo(
                    relativeBlockInfo.pos(),
                    Blocks.AIR.defaultBlockState(),
                    relativeBlockInfo.nbt()
            );
        }

        // Light blue wool -> biome-appropriate terrain substitution
        if (block == Blocks.LIGHT_BLUE_WOOL) {
            BlockState replacement = getBiomeTerrainBlock(level, relativeBlockInfo.pos());
            return new StructureTemplate.StructureBlockInfo(
                    relativeBlockInfo.pos(),
                    replacement,
                    null
            );
        }

        // Magenta wool -> dirt path
        if (block == Blocks.MAGENTA_WOOL) {
            return new StructureTemplate.StructureBlockInfo(
                    relativeBlockInfo.pos(),
                    Blocks.DIRT_PATH.defaultBlockState(),
                    null
            );
        }

        // Yellow wool -> planks (culture-neutral building material)
        if (block == Blocks.YELLOW_WOOL) {
            return new StructureTemplate.StructureBlockInfo(
                    relativeBlockInfo.pos(),
                    Blocks.OAK_PLANKS.defaultBlockState(),
                    null
            );
        }

        return relativeBlockInfo;
    }

    /**
     * Returns a biome-appropriate terrain block for the given position.
     * Falls back to grass block if the biome cannot be determined.
     */
    private BlockState getBiomeTerrainBlock(LevelReader level, BlockPos pos) {
        try {
            Biome biome = level.getBiome(pos).value();
            // Check world block below to infer terrain
            BlockState below = level.getBlockState(pos.below());
            if (below.is(BlockTags.SAND)) {
                return Blocks.SAND.defaultBlockState();
            }
            if (below.is(Blocks.RED_SAND)) {
                return Blocks.RED_SAND.defaultBlockState();
            }
            if (below.is(Blocks.SNOW_BLOCK) || below.is(Blocks.POWDER_SNOW)) {
                return Blocks.SNOW_BLOCK.defaultBlockState();
            }
            if (below.is(Blocks.MYCELIUM)) {
                return Blocks.MYCELIUM.defaultBlockState();
            }
            if (below.is(Blocks.PODZOL)) {
                return Blocks.PODZOL.defaultBlockState();
            }
        } catch (Exception ignored) {
            // Fall through to default
        }
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }

    @Override
    public StructureProcessorType<?> getType() {
        return ColonyWorldGenRegistry.COLONY_STRUCTURE_PROCESSOR.get();
    }
}
