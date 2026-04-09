package com.ultra.megamod.feature.economy;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.economy.network.OpenAtmPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class AtmBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<AtmBlock> CODEC = AtmBlock.simpleCodec(AtmBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    // Bottom half: solid base machine body
    private static final VoxelShape SHAPE_BOTTOM_NS = Shapes.or(
        Block.box(2, 0, 3, 14, 16, 13)
    );
    private static final VoxelShape SHAPE_BOTTOM_EW = Shapes.or(
        Block.box(3, 0, 2, 13, 16, 14)
    );

    // Top half: screen housing (slightly narrower)
    private static final VoxelShape SHAPE_TOP_NS = Shapes.or(
        Block.box(2, 0, 3, 14, 12, 13)
    );
    private static final VoxelShape SHAPE_TOP_EW = Shapes.or(
        Block.box(3, 0, 2, 13, 12, 14)
    );

    public AtmBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    protected MapCodec<? extends AtmBlock> codec() {
        return CODEC;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxY() && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
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

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            EconomyManager eco = EconomyManager.get((ServerLevel) level);
            int wallet = eco.getWallet(player.getUUID());
            int bank = eco.getBank(player.getUUID());
            PacketDistributor.sendToPlayer(serverPlayer, (CustomPacketPayload) new OpenAtmPayload(wallet, bank), (CustomPacketPayload[]) new CustomPacketPayload[0]);
        }
        return InteractionResult.SUCCESS;
    }
}
