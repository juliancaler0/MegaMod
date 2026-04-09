package com.ultra.megamod.feature.dungeons.generation;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;

/**
 * Port of DNL's WaterloggingFixProcessor.
 * Removes waterlogging from blocks that were waterlogged in the template
 * but placed in air (non-water) positions.
 */
public class WaterloggingFixProcessor extends StructureProcessor {

    public static final WaterloggingFixProcessor INSTANCE = new WaterloggingFixProcessor();
    public static final MapCodec<WaterloggingFixProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    private WaterloggingFixProcessor() {}

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level,
            BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo,
            StructureTemplate.StructureBlockInfo relativeBlockInfo,
            StructurePlaceSettings settings) {
        BlockState state = relativeBlockInfo.state();
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)
                && state.getValue(BlockStateProperties.WATERLOGGED)) {
            // Check if the world position actually has water
            BlockState worldState = level.getBlockState(relativeBlockInfo.pos());
            if (!worldState.getFluidState().isSource()) {
                return new StructureTemplate.StructureBlockInfo(
                        relativeBlockInfo.pos(),
                        state.setValue(BlockStateProperties.WATERLOGGED, false),
                        relativeBlockInfo.nbt()
                );
            }
        }
        return relativeBlockInfo;
    }

    @Override
    public StructureProcessorType<?> getType() {
        return DungeonProcessorRegistry.WATERLOGGING_FIX_PROCESSOR.get();
    }
}
