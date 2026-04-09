package com.ultra.megamod.feature.dungeons.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BookPileBlock extends PileBlock {
    public static final MapCodec<BookPileBlock> CODEC = BookPileBlock.simpleCodec(BookPileBlock::new);

    protected static final VoxelShape BOOK_ONE_AABB = Block.box(0, 0, 0, 16, 3, 16);
    protected static final VoxelShape BOOK_TWO_AABB = Block.box(0, 0, 0, 16, 8, 16);
    protected static final VoxelShape BOOK_THREE_AABB = Block.box(0, 0, 0, 16, 10, 16);
    protected static final VoxelShape BOOK_FOUR_AABB = Block.box(0, 0, 0, 16, 10, 16);

    public BookPileBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BookPileBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(PILE)) {
            case 2 -> BOOK_TWO_AABB;
            case 3 -> BOOK_THREE_AABB;
            case 4 -> BOOK_FOUR_AABB;
            default -> BOOK_ONE_AABB;
        };
    }
}
