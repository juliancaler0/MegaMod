package com.ultra.megamod.feature.citizen.multipiston;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Multi-Piston — configurable block mover. Right-click opens configuration screen.
 * Redstone activation triggers push (signal on) or pull (signal off).
 */
public class MultiPistonBlock extends Block implements EntityBlock {
    public static final MapCodec<MultiPistonBlock> CODEC = MultiPistonBlock.simpleCodec(MultiPistonBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    /** Client-side screen opener — populated by {@code MultiPistonClientProxy} on the client.
     *  Leaving it as a no-op on the dedicated server keeps this class free of client imports. */
    public static java.util.function.Consumer<BlockPos> OPEN_CONFIG_SCREEN = pos -> {};

    public MultiPistonBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends MultiPistonBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING, POWERED});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
            .setValue(FACING, context.getNearestLookingDirection().getOpposite())
            .setValue(POWERED, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MultiPistonBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        if (type == MultiPistonRegistry.MULTI_PISTON_BE.get()) {
            return (lvl, pos, st, be) -> MultiPistonBlockEntity.serverTick(lvl, pos, st, (MultiPistonBlockEntity) be);
        }
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            // Open the configuration screen via the client proxy (registered on client-side init).
            OPEN_CONFIG_SCREEN.accept(pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide()) return;
        boolean powered = level.hasNeighborSignal(pos);
        boolean wasPowered = state.getValue(POWERED);

        if (powered != wasPowered) {
            level.setBlock(pos, state.setValue(POWERED, powered), 3);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MultiPistonBlockEntity piston) {
                if (powered) {
                    piston.startPush();
                } else {
                    piston.startPull();
                }
            }
        }
    }
}
