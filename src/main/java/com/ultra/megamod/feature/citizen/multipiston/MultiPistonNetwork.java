package com.ultra.megamod.feature.citizen.multipiston;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers the Multi-Piston network payload.
 */
public class MultiPistonNetwork {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod").versioned("1.0").optional();

        registrar.playToServer(
            MultiPistonPayload.TYPE,
            MultiPistonPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    BlockPos pos = payload.pos();
                    // Validate distance
                    if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0) {
                        return;
                    }
                    BlockEntity be = player.level().getBlockEntity(pos);
                    if (be instanceof MultiPistonBlockEntity piston) {
                        piston.configure(payload.inputDir(), payload.outputDir(), payload.range(), payload.speed());
                    }
                }
            })
        );
    }
}
