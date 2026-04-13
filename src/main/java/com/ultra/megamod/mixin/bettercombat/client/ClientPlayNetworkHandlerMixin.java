package com.ultra.megamod.mixin.bettercombat.client;

import com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optionally suppresses vanilla sweep attack particles when BetterCombat replaces sweeping.
 * Ported 1:1 from BetterCombat (net.bettercombat.mixin.client.ClientPlayNetworkHandlerMixin).
 */
@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "handleParticleEvent", at = @At("HEAD"), cancellable = true)
    private void bettercombat$handleParticleEvent_Pre(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
        // Suppress sweep particles when BetterCombat sweep is disabled
        if (!BetterCombatConfig.allow_vanilla_sweeping
                && packet.getParticle().getType().equals(ParticleTypes.SWEEP_ATTACK)) {
            ci.cancel();
        }
    }
}
