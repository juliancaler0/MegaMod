package com.ultra.megamod.feature.casino.blackjack;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.casino.CasinoManager;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import org.jetbrains.annotations.Nullable;

public class BlackjackTableBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<BlackjackTableBlock> CODEC = BlackjackTableBlock.simpleCodec(BlackjackTableBlock::new);

    // Table-height collision shape (12/16 tall)
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public BlackjackTableBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlackjackTableBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlackjackTableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof BlackjackTableBlockEntity bjBE) {
                BlackjackTableBlockEntity.serverTick(lvl, pos, st, bjBE);
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BlackjackTableBlockEntity tableBE)) {
            return InteractionResult.FAIL;
        }

        BlackjackTable game = tableBE.getOrCreateGame();
        ServerLevel serverLevel = (ServerLevel) player.level();

        if (!game.isPlayerSeated(player.getUUID())) {
            boolean joined = game.joinTable(serverPlayer);
            if (joined) {
                serverPlayer.sendSystemMessage(
                        Component.literal("You joined the blackjack table! Place a bet to start.")
                                .withStyle(ChatFormatting.GREEN));
            } else {
                serverPlayer.sendSystemMessage(
                        Component.literal("The table is full!")
                                .withStyle(ChatFormatting.RED));
            }
        } else {
            // Player is already seated -- show current game state info
            BlackjackTable.Phase phase = game.getPhase();
            serverPlayer.sendSystemMessage(
                    Component.literal("Blackjack - Phase: " + phase.name() + " | Players: " + game.getSeatCount() + "/4")
                            .withStyle(ChatFormatting.GOLD));
        }

        return InteractionResult.CONSUME;
    }
}
