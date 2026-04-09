package com.ultra.megamod.feature.furniture;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class DungeonChestBlock extends FurnitureBlock implements EntityBlock {
    public static final MapCodec<DungeonChestBlock> CODEC = DungeonChestBlock.simpleCodec(DungeonChestBlock::new);

    public DungeonChestBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    protected MapCodec<? extends DungeonChestBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DungeonChestBlockEntity(pos, state);
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DungeonChestBlockEntity chest) {
            // Generate loot on first open using player's Luck and loot_fortune
            if (chest.hasPendingLoot()) {
                chest.generateLootForPlayer(player);
            }
            ((ServerPlayer) player).openMenu(new SimpleMenuProvider(
                    (containerId, playerInv, p) -> ChestMenu.sixRows(containerId, playerInv, chest),
                    Component.literal("Dungeon Chest")
            ));
        }
        return InteractionResult.CONSUME;
    }

    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        // Drop contents if block is replaced with a different block type
        if (!oldState.is(state.getBlock()) && !oldState.isAir()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DungeonChestBlockEntity chest) {
                chest.dropContents(level, pos);
            }
        }
    }

    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DungeonChestBlockEntity chest) {
            chest.dropContents(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
