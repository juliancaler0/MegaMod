package com.ultra.megamod.feature.citizen.colonyblocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Stash block — a simple colony storage container (like a Rack).
 * Functions as a 27-slot container for temporary item storage,
 * typically placed inside Town Hall for citizen deliveries.
 */
public class BlockStash extends Block implements EntityBlock {
    public static final MapCodec<BlockStash> CODEC = BlockStash.simpleCodec(BlockStash::new);

    public BlockStash(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends BlockStash> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityStash(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileEntityStash stash) {
            ((ServerPlayer) player).openMenu(new SimpleMenuProvider(
                (containerId, playerInv, p) -> ChestMenu.threeRows(containerId, playerInv, stash),
                Component.literal("Colony Stash")
            ));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileEntityStash stash) {
            if (!level.isClientSide()) {
                stash.dropContents(level, pos);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
