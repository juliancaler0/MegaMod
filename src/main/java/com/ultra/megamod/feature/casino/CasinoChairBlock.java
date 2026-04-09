package com.ultra.megamod.feature.casino;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.furniture.FurnitureBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Base casino chair - sits players on right-click using invisible armor stand.
 * Subclasses add game-specific behavior after sitting.
 */
public class CasinoChairBlock extends FurnitureBlock {
    public static final MapCodec<CasinoChairBlock> CODEC = CasinoChairBlock.simpleCodec(CasinoChairBlock::new);

    public CasinoChairBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends CasinoChairBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isPassenger()) {
            return InteractionResult.CONSUME;
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        ServerLevel serverLevel = (ServerLevel) level;

        // Sit the player
        ArmorStand seat = new ArmorStand(EntityType.ARMOR_STAND, serverLevel);
        // Sit at roughly half-block height (chair is 1 block tall, player eyes at ~1.6 blocks)
        // ArmorStand riding offset puts player ~1.4 blocks above stand position
        // So stand at y-1.0 puts player's butt at about y+0.4 (seat height)
        seat.setPos(pos.getX() + 0.5, pos.getY() - 1.0, pos.getZ() + 0.5);
        seat.setInvisible(true);
        seat.setNoGravity(true);
        seat.setInvulnerable(true);
        seat.setNoBasePlate(true);
        seat.setSilent(true);
        seat.addTag("megamod_casino_seat");
        serverLevel.addFreshEntity(seat);
        serverPlayer.startRiding(seat);

        // Subclass hook
        onPlayerSat(serverPlayer, serverLevel, pos);

        return InteractionResult.CONSUME;
    }

    /**
     * Called after the player sits down. Detects nearby game tables and opens the GUI.
     * Scans 3 blocks in each direction for recognized game table furniture blocks,
     * then does a wider scan (12 blocks) for the wheel since it can be further away.
     */
    protected void onPlayerSat(ServerPlayer player, ServerLevel level, BlockPos chairPos) {
        // Scan for nearby game table blocks (3-block range)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos checkPos = chairPos.offset(dx, dy, dz);
                    net.minecraft.world.level.block.Block block = level.getBlockState(checkPos).getBlock();

                    // Roulette table
                    if (block == com.ultra.megamod.feature.furniture.FurnitureRegistry.CASINO_TABLE_ROULETTE.get()) {
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                                new com.ultra.megamod.feature.casino.network.RouletteGameSyncPayload("{\"phase\":\"BETTING\",\"timer\":300}"));
                        return;
                    }

                    // Craps table
                    if (block == com.ultra.megamod.feature.furniture.FurnitureRegistry.CASINO_TABLE_CRAPS.get()) {
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                                new com.ultra.megamod.feature.casino.network.CrapsGameSyncPayload("{\"phase\":\"BETTING\",\"die1\":0,\"die2\":0,\"point\":0,\"betAmount\":0,\"resultMessage\":\"\"}"));
                        return;
                    }

                    // Baccarat table (blank table used for baccarat)
                    if (block == com.ultra.megamod.feature.furniture.FurnitureRegistry.CASINO_TABLE_BLANK.get()) {
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                                new com.ultra.megamod.feature.casino.network.BaccaratGameSyncPayload("{\"phase\":\"BETTING\"}"));
                        return;
                    }
                }
            }
        }

        // Wider scan for the wheel block (12 blocks) — the wheel is typically
        // mounted high on a wall, further from chairs than table games.
        for (int dx = -12; dx <= 12; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dz = -12; dz <= 12; dz++) {
                    BlockPos checkPos = chairPos.offset(dx, dy, dz);
                    if (level.getBlockState(checkPos).getBlock() instanceof com.ultra.megamod.feature.casino.wheel.WheelBlock) {
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                                new com.ultra.megamod.feature.casino.network.OpenWheelPayload(checkPos));
                        return;
                    }
                }
            }
        }
    }
}
