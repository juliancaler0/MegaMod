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
 * Chaos Spawner barrier vertex block with FACING + BARRIER_VERTEXS properties.
 */
public class ChaosBarrierVertexBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ChaosBarrierVertexBlock> CODEC = ChaosBarrierVertexBlock.simpleCodec(ChaosBarrierVertexBlock::new);
    public static final EnumProperty<ChaosProperties.BarrierVertexs> BARRIER_VERTEX = ChaosProperties.BARRIER_VERTEXS;

    public ChaosBarrierVertexBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(BARRIER_VERTEX, ChaosProperties.BarrierVertexs.TOP_RIGHT));
    }

    @Override
    protected MapCodec<? extends ChaosBarrierVertexBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BARRIER_VERTEX);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue((Property) FACING, (Comparable) ctx.getHorizontalDirection());
    }
}
