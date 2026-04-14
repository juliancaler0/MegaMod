package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.rogues.effect.StealthEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/**
 * Server-side event handler for the Stealth spell effect.
 * <p>
 * - Prevents mobs from targeting stealthed players (unless within 2 blocks).
 * - Breaks stealth when the stealthed player attacks an entity.
 * - Plays a smoke-puff and stealth-leave sound whenever STEALTH is removed.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class StealthHandler {

    /**
     * When a mob tries to target a player who has the STEALTH effect,
     * cancel the targeting unless the mob is within 2 blocks (very close detection).
     */
    @SubscribeEvent
    public static void onMobTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof ServerPlayer target) {
            if (target.hasEffect(SpellEffects.STEALTH)) {
                double dist = event.getEntity().distanceTo(target);
                if (dist > 2.0) {
                    event.setNewAboutToBeSetTarget(null);
                }
            }
        }
    }

    /**
     * When a stealthed player attacks, break stealth by removing both
     * STEALTH and STEALTH_SPEED effects.
     */
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.hasEffect(SpellEffects.STEALTH)) {
                player.removeEffect(SpellEffects.STEALTH);
                player.removeEffect(SpellEffects.STEALTH_SPEED);
            }
        }
    }

    /**
     * When STEALTH is removed (by attack, expiration, or any other source),
     * spawn a smoke puff and play the stealth-leave sound.
     * Mirrors Rogues' {@code OnRemoval.configure(STEALTH...)} behaviour.
     */
    @SubscribeEvent
    public static void onStealthRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect().value() == SpellEffects.STEALTH.value()) {
            LivingEntity entity = event.getEntity();
            StealthEffect.onRemove(entity);
            // Companion STEALTH_SPEED should always be cleared with STEALTH.
            if (entity.hasEffect(SpellEffects.STEALTH_SPEED)) {
                entity.removeEffect(SpellEffects.STEALTH_SPEED);
            }
        }
    }

    /**
     * Mirror of {@link #onStealthRemoved(MobEffectEvent.Remove)} for natural expiration.
     */
    @SubscribeEvent
    public static void onStealthExpired(MobEffectEvent.Expired event) {
        var instance = event.getEffectInstance();
        if (instance != null && instance.getEffect().value() == SpellEffects.STEALTH.value()) {
            LivingEntity entity = event.getEntity();
            StealthEffect.onRemove(entity);
            if (entity.hasEffect(SpellEffects.STEALTH_SPEED)) {
                entity.removeEffect(SpellEffects.STEALTH_SPEED);
            }
        }
    }
}
