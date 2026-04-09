package com.ultra.megamod.feature.dungeons.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Chaos Spawner barrier edge block with FACING + BARRIER_EDGES properties.
 */
public class ChaosBarrierEdgeBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ChaosBarrierEdgeBlock> CODEC = ChaosBarrierEdgeBlock.simpleCodec(ChaosBarrierEdgeBlock::new);
    public static final EnumProperty<ChaosProperties.BarrierEdges> BARRIER_EDGE = ChaosProperties.BARRIER_EDGES;

    public ChaosBarrierEdgeBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(BARRIER_EDGE, ChaosProperties.BarrierEdges.UP));
    }

    @Override
    protected MapCodec<? extends ChaosBarrierEdgeBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BARRIER_EDGE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue((Property) FACING, (Comparable) ctx.getHorizontalDirection());
    }
}
