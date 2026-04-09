package com.ultra.megamod.feature.recovery;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GravestoneBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<GravestoneBlock> CODEC = GravestoneBlock.simpleCodec(GravestoneBlock::new);
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 12.0, 15.0);

    public GravestoneBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue((Property) FACING, (Comparable) Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends GravestoneBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GravestoneBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof GravestoneBlockEntity grave) {
                grave.tick(lvl);
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof GravestoneBlockEntity grave)) return InteractionResult.PASS;

        ServerPlayer serverPlayer = (ServerPlayer) player;
        boolean isOwner = grave.getOwnerUuid() != null && grave.getOwnerUuid().equals(player.getUUID());
        boolean isAdmin = AdminSystem.isAdmin(serverPlayer);

        if (isOwner || isAdmin) {
            grave.dropAllItems(level, pos);
            level.removeBlock(pos, false);
            player.displayClientMessage(Component.literal("\u00A7aYou recovered your items from the gravestone."), true);
            return InteractionResult.SUCCESS;
        } else {
            String ownerName = grave.getOwnerName() != null ? grave.getOwnerName() : "Unknown";
            player.displayClientMessage(Component.literal("\u00A7cThis gravestone belongs to " + ownerName + "."), true);
            return InteractionResult.FAIL;
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof GravestoneBlockEntity grave) {
            grave.dropAllItems(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
