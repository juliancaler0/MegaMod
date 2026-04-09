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

public class TrashBinBlock extends FurnitureBlock implements EntityBlock {
    public static final MapCodec<TrashBinBlock> CODEC = TrashBinBlock.simpleCodec(TrashBinBlock::new);

    public TrashBinBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    protected MapCodec<? extends TrashBinBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrashBinBlockEntity(pos, state);
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TrashBinBlockEntity bin) {
            ((ServerPlayer) player).openMenu(new SimpleMenuProvider(
                    (containerId, playerInv, p) -> ChestMenu.threeRows(containerId, playerInv, bin),
                    Component.literal("Trash Bin")
            ));
        }
        return InteractionResult.CONSUME;
    }
}
