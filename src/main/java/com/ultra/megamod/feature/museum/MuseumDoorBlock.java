package com.ultra.megamod.feature.museum;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.museum.dimension.MuseumPortalHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MuseumDoorBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<MuseumDoorBlock> CODEC = MuseumDoorBlock.simpleCodec(MuseumDoorBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    // Bottom half shapes: base + pillar lower portions
    private static final VoxelShape SHAPE_BOTTOM_NS = Shapes.or(
        Block.box(0, 0, 4, 16, 2, 12),    // base
        Block.box(0, 2, 5, 3, 16, 11),     // left pillar
        Block.box(13, 2, 5, 16, 16, 11)    // right pillar
    );
    private static final VoxelShape SHAPE_BOTTOM_EW = Shapes.or(
        Block.box(4, 0, 0, 12, 2, 16),
        Block.box(5, 2, 0, 11, 16, 3),
        Block.box(5, 2, 13, 11, 16, 16)
    );

    // Top half shapes: pillar upper portions + arch
    private static final VoxelShape SHAPE_TOP_NS = Shapes.or(
        Block.box(0, 0, 5, 3, 14, 11),     // left pillar
        Block.box(13, 0, 5, 16, 14, 11),   // right pillar
        Block.box(0, 14, 5, 16, 16, 11)    // arch top
    );
    private static final VoxelShape SHAPE_TOP_EW = Shapes.or(
        Block.box(5, 0, 0, 11, 14, 3),
        Block.box(5, 0, 13, 11, 14, 16),
        Block.box(5, 14, 0, 11, 16, 16)
    );

    public MuseumDoorBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    protected MapCodec<? extends MuseumDoorBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MuseumDoorBlockEntity(pos, state);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxY() && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null; // can't place if no room above
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(this) && otherState.getValue(HALF) != half) {
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 35);
                level.levelEvent(player, 2001, otherPos, Block.getId(otherState));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING, HALF});
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        boolean isNS = facing == Direction.NORTH || facing == Direction.SOUTH;
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return isNS ? SHAPE_BOTTOM_NS : SHAPE_BOTTOM_EW;
        } else {
            return isNS ? SHAPE_TOP_NS : SHAPE_TOP_EW;
        }
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (level.dimension().equals(MegaModDimensions.MUSEUM)) {
                serverPlayer.sendSystemMessage(Component.literal("You are already in your museum! Use the portal to leave.").withStyle(ChatFormatting.YELLOW));
                return InteractionResult.FAIL;
            }
            if (level.dimension().equals(MegaModDimensions.DUNGEON)) {
                serverPlayer.sendSystemMessage(Component.literal("You cannot enter the museum from inside a dungeon!").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
            MuseumPortalHandler.handleEnterMuseum(serverPlayer);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
