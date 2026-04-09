package com.ultra.megamod.feature.casino;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.casino.network.OpenWheelPayload;
import com.ultra.megamod.feature.casino.wheel.WheelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Casino chair for the big wheel. When a player sits, it finds the nearest
 * wheel block and opens the betting screen.
 */
public class WheelChairBlock extends CasinoChairBlock {
    public static final MapCodec<WheelChairBlock> WC_CODEC = WheelChairBlock.simpleCodec(WheelChairBlock::new);

    public WheelChairBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends WheelChairBlock> codec() {
        return WC_CODEC;
    }

    @Override
    protected void onPlayerSat(ServerPlayer player, ServerLevel level, BlockPos chairPos) {
        // Search nearby for a wheel block (within 6 blocks)
        BlockPos wheelPos = findNearbyWheel(level, chairPos);
        if (wheelPos != null) {
            PacketDistributor.sendToPlayer(player, new OpenWheelPayload(wheelPos));
        }
    }

    private BlockPos findNearbyWheel(ServerLevel level, BlockPos chairPos) {
        for (int dx = -12; dx <= 12; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dz = -12; dz <= 12; dz++) {
                    BlockPos check = chairPos.offset(dx, dy, dz);
                    if (level.getBlockState(check).getBlock() instanceof WheelBlock) {
                        return check;
                    }
                }
            }
        }
        return null;
    }
}
