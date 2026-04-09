package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.MegaMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/**
 * Server-side event handler for the Stealth spell effect.
 * <p>
 * - Prevents mobs from targeting stealthed players (unless within 2 blocks).
 * - Breaks stealth when the stealthed player attacks an entity.
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
}
