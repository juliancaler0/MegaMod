package com.ultra.megamod.feature.combat.items;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Decorative workbench block with horizontal facing and tooltip.
 * Used for Archers, Rogues, Jewelers, and Monk workbenches.
 * Ported 1:1 from the RPG series ref mods.
 */
public class WorkbenchBlock extends HorizontalDirectionalBlock {

    private static final MapCodec<WorkbenchBlock> CODEC = simpleCodec(WorkbenchBlock::new);

    private final String hintKey;
    @Nullable
    private final VoxelShape customShape;

    public WorkbenchBlock(Properties properties, String hintKey, @Nullable VoxelShape customShape) {
        super(properties);
        this.hintKey = hintKey;
        this.customShape = customShape;
        this.registerDefaultState(this.stateDefinition.any().setValue((Property) FACING, (Comparable) Direction.NORTH));
    }

    public WorkbenchBlock(Properties properties) {
        this(properties, "", null);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return customShape != null ? customShape : super.getShape(state, level, pos, context);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    public String getHintKey() {
        return hintKey;
    }
}
