package com.ultra.megamod.feature.museum;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PedestalBlock extends Block {
    public static final MapCodec<PedestalBlock> CODEC = PedestalBlock.simpleCodec(PedestalBlock::new);

    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(2, 0, 2, 14, 2, 14),    // wide base
        Block.box(4, 2, 4, 12, 11, 12),   // center column
        Block.box(2, 11, 2, 14, 13, 14)   // top platform
    );

    public PedestalBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends PedestalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
