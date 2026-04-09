package com.ultra.megamod.feature.citizen.colonyblocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Colony flag banner block — decorative banner that shows the colony's faction color.
 * Stores a color index (0-15) representing the faction dye color.
 * Right-click cycles through colors.
 */
public class BlockColonyBanner extends HorizontalDirectionalBlock {
    public static final MapCodec<BlockColonyBanner> CODEC = BlockColonyBanner.simpleCodec(BlockColonyBanner::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 15);

    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

    public BlockColonyBanner(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(COLOR, 0));
    }

    @Override
    protected MapCodec<? extends BlockColonyBanner> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING, COLOR});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        int currentColor = state.getValue(COLOR);
        int nextColor = (currentColor + 1) % 16;
        level.setBlock(pos, state.setValue(COLOR, nextColor), 3);

        String colorName = getDyeColorName(nextColor);
        ((ServerPlayer) player).displayClientMessage(
            Component.literal("\u00a7eBanner color set to: " + colorName), true);

        return InteractionResult.CONSUME;
    }

    private static String getDyeColorName(int index) {
        return switch (index) {
            case 0 -> "White";
            case 1 -> "Orange";
            case 2 -> "Magenta";
            case 3 -> "Light Blue";
            case 4 -> "Yellow";
            case 5 -> "Lime";
            case 6 -> "Pink";
            case 7 -> "Gray";
            case 8 -> "Light Gray";
            case 9 -> "Cyan";
            case 10 -> "Purple";
            case 11 -> "Blue";
            case 12 -> "Brown";
            case 13 -> "Green";
            case 14 -> "Red";
            case 15 -> "Black";
            default -> "Unknown";
        };
    }
}
