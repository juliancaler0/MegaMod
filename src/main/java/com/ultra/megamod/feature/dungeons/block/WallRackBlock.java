package com.ultra.megamod.feature.dungeons.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallRackBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<WallRackBlock> CODEC = WallRackBlock.simpleCodec(WallRackBlock::new);
    private static final VoxelShape SHAPE_NS = Block.box(1, 2, 6, 15, 14, 10);
    private static final VoxelShape SHAPE_EW = Block.box(6, 2, 1, 10, 14, 15);

    public WallRackBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState((BlockState) this.stateDefinition.any().setValue((Property) FACING, (Comparable) Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends WallRackBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState) this.defaultBlockState().setValue((Property) FACING, (Comparable) ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        Direction facing = state.getValue(FACING);
        return (facing == Direction.NORTH || facing == Direction.SOUTH) ? SHAPE_NS : SHAPE_EW;
    }
}
