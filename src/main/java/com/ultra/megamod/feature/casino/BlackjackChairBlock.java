package com.ultra.megamod.feature.casino;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.casino.blackjack.BlackjackTable;
import com.ultra.megamod.feature.casino.blackjack.BlackjackTableBlockEntity;
import com.ultra.megamod.feature.casino.network.BlackjackSyncPayload;
import com.ultra.megamod.feature.casino.network.OpenBlackjackPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Casino chair for blackjack tables. When a player sits, it finds the nearest
 * blackjack table block and joins the game.
 */
public class BlackjackChairBlock extends CasinoChairBlock {
    public static final MapCodec<BlackjackChairBlock> BJ_CODEC = BlackjackChairBlock.simpleCodec(BlackjackChairBlock::new);

    public BlackjackChairBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends BlackjackChairBlock> codec() {
        return BJ_CODEC;
    }

    @Override
    protected void onPlayerSat(ServerPlayer player, ServerLevel level, BlockPos chairPos) {
        // Search nearby for a blackjack table block (within 4 blocks)
        BlockPos tablePos = findNearbyTable(level, chairPos);
        if (tablePos == null) {
            player.displayClientMessage(Component.literal("No blackjack table nearby!"), true);
            return;
        }

        BlockEntity be = level.getBlockEntity(tablePos);
        if (be instanceof BlackjackTableBlockEntity tableBE) {
            BlackjackTable game = tableBE.getOrCreateGame();
            if (game.isPlayerSeated(player.getUUID())) {
                // Already seated - reopen the screen
                PacketDistributor.sendToPlayer(player, new OpenBlackjackPayload(tablePos));
                BlackjackSyncPayload sync = new BlackjackSyncPayload(game.toJsonForPlayer(player.getUUID()).toString());
                PacketDistributor.sendToPlayer(player, sync);
            } else if (game.joinTable(player)) {
                // Open the blackjack screen and send initial game state
                PacketDistributor.sendToPlayer(player, new OpenBlackjackPayload(tablePos));
                BlackjackSyncPayload sync = new BlackjackSyncPayload(game.toJsonForPlayer(player.getUUID()).toString());
                PacketDistributor.sendToPlayer(player, sync);
            } else {
                player.displayClientMessage(Component.literal("This table is full!"), true);
            }
        }
    }

    /**
     * Searches within 4 blocks for a blackjack table block.
     */
    private BlockPos findNearbyTable(ServerLevel level, BlockPos chairPos) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos check = chairPos.offset(dx, dy, dz);
                    if (level.getBlockState(check).getBlock() instanceof com.ultra.megamod.feature.casino.blackjack.BlackjackTableBlock) {
                        return check;
                    }
                }
            }
        }
        return null;
    }
}
