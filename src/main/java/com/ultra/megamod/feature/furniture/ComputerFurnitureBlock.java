package com.ultra.megamod.feature.furniture;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.OpenComputerPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;

public class ComputerFurnitureBlock extends FurnitureBlock {
    public static final MapCodec<ComputerFurnitureBlock> CODEC = ComputerFurnitureBlock.simpleCodec(ComputerFurnitureBlock::new);

    public ComputerFurnitureBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    protected MapCodec<? extends ComputerFurnitureBlock> codec() {
        return CODEC;
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            boolean isAdmin = AdminSystem.isAdmin(serverPlayer);
            EconomyManager eco = EconomyManager.get((ServerLevel) level);
            int wallet = eco.getWallet(player.getUUID());
            int bank = eco.getBank(player.getUUID());
            PacketDistributor.sendToPlayer(serverPlayer, new OpenComputerPayload(isAdmin, wallet, bank));
        }
        return InteractionResult.SUCCESS;
    }
}
