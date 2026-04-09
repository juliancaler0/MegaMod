package com.ultra.megamod.feature.citizen.ornament;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Floating carpet block — a thin decorative carpet that does not require support below it.
 * Available in all 16 dye colors. Unlike vanilla carpets, these can be placed mid-air.
 * No block entity needed. Extends Block directly to avoid CarpetBlock constructor API changes.
 */
public class FloatingCarpetBlock extends Block {

    public static final MapCodec<FloatingCarpetBlock> CODEC = FloatingCarpetBlock.simpleCodec(FloatingCarpetBlock::new);

    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

    private final DyeColor dyeColor;

    public FloatingCarpetBlock(DyeColor color, BlockBehaviour.Properties props) {
        super(props);
        this.dyeColor = color;
    }

    /**
     * Properties-only constructor for codec/registration system. Uses WHITE as default color.
     */
    public FloatingCarpetBlock(BlockBehaviour.Properties props) {
        super(props);
        this.dyeColor = DyeColor.WHITE;
    }

    @Override
    protected MapCodec<? extends FloatingCarpetBlock> codec() {
        return CODEC;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * Allow placement without a supporting block below (floating).
     */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }
}
