package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.MegaMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Server-side event handler for spell effects that require damage interception.
 *
 * Issue 1: Frost Shield — absorbs the next incoming hit then pops.
 * Issue 2: Hunter's Mark Stash — transfers mark on next arrow hit.
 * Issue 6: Cast interruption — cancels charged/channeled casts when taking damage.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class SpellEffectHandler {

    /**
     * Issue 1: Frost Shield damage absorption.
     * When a player with FROST_SHIELD takes damage, absorb the hit completely
     * and remove the shield effect (it "pops").
     * Runs at HIGH priority so it intercepts before other damage handlers.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFrostShieldAbsorb(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (target.hasEffect(SpellEffects.FROST_SHIELD)) {
            // Absorb the hit completely
            event.setNewDamage(0);
            // Remove the effect (it pops)
            target.removeEffect(SpellEffects.FROST_SHIELD);
            // Visual/audio feedback — glass break sound
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.GLASS_BREAK, target.getSoundSource(), 1.0f, 1.2f);
        }
    }

    /**
     * Issue 2: Hunter's Mark Stash arrow-hit conversion.
     * When a player with HUNTERS_MARK_STASH shoots an arrow that hits a target,
     * transfer HUNTERS_MARK to the target and remove the stash from the shooter.
     */
    @SubscribeEvent
    public static void onArrowHitWithMark(LivingDamageEvent.Post event) {
        var source = event.getSource();
        if (source.getDirectEntity() instanceof AbstractArrow
                && source.getEntity() instanceof LivingEntity shooter) {
            if (shooter.hasEffect(SpellEffects.HUNTERS_MARK_STASH)) {
                // Transfer mark to the hit target — 12 seconds, amplifier 0
                event.getEntity().addEffect(new MobEffectInstance(
                    SpellEffects.HUNTERS_MARK, 240, 0));
                // Remove stash from shooter
                shooter.removeEffect(SpellEffects.HUNTERS_MARK_STASH);
            }
        }
    }

    /**
     * Issue 6: Cast interruption on damage.
     * When a player takes damage while casting a charged or channeled spell,
     * the cast is interrupted and the client cast bar is cleared.
     */
    @SubscribeEvent
    public static void onDamageCancelsCast(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (SpellCastManager.isCasting(player.getUUID())) {
                SpellCastManager.cancelCast(player.getUUID());
                // Notify client to clear cast bar
                PacketDistributor.sendToPlayer(player,
                    new SpellCastSyncPayload(false, "", 0, 0xFFFFFFFF));
            }
        }
    }
}
