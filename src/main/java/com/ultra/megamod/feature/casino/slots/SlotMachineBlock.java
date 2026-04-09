package com.ultra.megamod.feature.casino.slots;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.casino.network.OpenSlotMachinePayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class SlotMachineBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<SlotMachineBlock> CODEC = SlotMachineBlock.simpleCodec(SlotMachineBlock::new);
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public SlotMachineBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState((BlockState) this.stateDefinition.any().setValue((Property) FACING, (Comparable) Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends SlotMachineBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState) this.defaultBlockState().setValue((Property) FACING, (Comparable) ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SlotMachineBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SlotMachineBlockEntity slotBE) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            java.util.UUID playerId = serverPlayer.getUUID();

            // Check if occupied by another player
            if (slotBE.isOccupied() && !slotBE.isUsedBy(playerId)) {
                serverPlayer.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("This slot machine is in use by another player!"),
                        true
                );
                return InteractionResult.CONSUME;
            }

            // Lock to this player and open the slot machine screen
            slotBE.occupy(playerId);

            // Send payload to open the screen on the client
            int wallet = com.ultra.megamod.feature.casino.chips.ChipManager.get((ServerLevel) level).getBalance(playerId);
            PacketDistributor.sendToPlayer(serverPlayer,
                    new OpenSlotMachinePayload(pos, slotBE.getBetIndex(), slotBE.getLineMode(), wallet));
        }

        return InteractionResult.CONSUME;
    }
}
