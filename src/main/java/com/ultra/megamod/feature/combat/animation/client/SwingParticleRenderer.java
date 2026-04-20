package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.MegaMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Per-tick driver for {@link PlayerAttackAnimatable#updateAnimationsOnTick} and
 * BetterCombat's movement-speed attenuation during attacks.
 *
 * <p>Originally this class also spawned its own port-invented slash particles via
 * {@link com.ultra.megamod.feature.combat.spell.client.particle.SpellParticleRegistry}.
 * That system has been removed in favor of the proper source port: particles are now
 * owned by {@code BetterCombatParticles} and spawned via {@code SlashParticleUtil} from
 * the attack flow (see {@code MinecraftMixin.megamod$startUpswing} /
 * {@code AbstractClientPlayerMixin.updateAnimationsOnTick}).</p>
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class SwingParticleRenderer {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // Drive BetterCombat per-tick animation updates (poses, scheduled particles) for every
        // client-side player entity. This is the sole hook — AvatarRendererMixin delegates here.
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof PlayerAttackAnimatable animatable) {
                animatable.updateAnimationsOnTick();
            }
        }

        // Smooth movement-speed reduction while attacking (ported from BC's ClientPlayerEntityMixin).
        var bcClient = (com.ultra.megamod.feature.combat.animation.api.MinecraftClient_BetterCombat) mc;
        float speedMult = (float) Math.min(Math.max(
                com.ultra.megamod.feature.combat.animation.config.ScopedCombatConfig.movementSpeedWhileAttacking(mc.player), 0), 1);
        if (speedMult < 1.0f && !mc.player.isPassenger()) {
            float swingProgress = bcClient.getSwingProgress();
            if (swingProgress < 0.98f) {
                if (com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.movement_speed_applied_smoothly) {
                    double p2;
                    if (swingProgress <= 0.5) {
                        p2 = com.ultra.megamod.feature.combat.animation.utils.MathHelper.easeOutCubic(swingProgress * 2);
                    } else {
                        p2 = com.ultra.megamod.feature.combat.animation.utils.MathHelper.easeOutCubic(1 - ((swingProgress - 0.5) * 2));
                    }
                    speedMult = (float) (1.0 - (1.0 - speedMult) * p2);
                }
                var vel = mc.player.getDeltaMovement();
                mc.player.setDeltaMovement(vel.x * speedMult, vel.y, vel.z * speedMult);
            }
        }
    }
}
