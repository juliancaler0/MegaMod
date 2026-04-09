package com.ultra.megamod.feature.dungeons.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Chaos Spawner edge/diamond edge block with FACING + ALL_SIDES properties.
 * Used for the boss room cage structure in DNL dungeons.
 */
public class ChaosEdgeBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ChaosEdgeBlock> CODEC = ChaosEdgeBlock.simpleCodec(ChaosEdgeBlock::new);
    public static final EnumProperty<ChaosProperties.AllSides> ALL_SIDES = ChaosProperties.ALL_SIDES;

    public ChaosEdgeBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ALL_SIDES, ChaosProperties.AllSides.BOTTOM));
    }

    @Override
    protected MapCodec<? extends ChaosEdgeBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ALL_SIDES);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue((Property) FACING, (Comparable) ctx.getHorizontalDirection());
    }
}
