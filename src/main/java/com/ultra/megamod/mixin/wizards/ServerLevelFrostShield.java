package com.ultra.megamod.mixin.wizards;

import com.ultra.megamod.feature.combat.wizards.effect.FrostShielded;
import com.ultra.megamod.feature.combat.wizards.util.SoundHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelFrostShield {
    // EntityStatuses.BLOCK_WITH_SHIELD = 29
    private static final byte BLOCK_WITH_SHIELD = 29;

    @Inject(method = "broadcastEntityEvent", at = @At("HEAD"), cancellable = true)
    private void broadcastEntityEvent_HEAD_FrostShield(Entity entity, byte status, CallbackInfo ci) {
        if (status == BLOCK_WITH_SHIELD && entity instanceof FrostShielded shielded) {
            if (shielded.hasFrostShield()) {
                SoundHelper.playSoundEvent(entity.level(), entity,
                        SoundEvent.createVariableRangeEvent(
                                Identifier.fromNamespaceAndPath("megamod", "combat.frost_shield_impact")));
                ci.cancel();
            }
        }
    }
}
