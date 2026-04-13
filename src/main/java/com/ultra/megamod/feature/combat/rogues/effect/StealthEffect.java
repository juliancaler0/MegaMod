package com.ultra.megamod.feature.combat.rogues.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Stealth effect that grants invisibility-like behavior.
 * When removed, plays a smoke puff particle effect and sound.
 * Ported from net.rogues.effect.StealthEffect.
 */
public class StealthEffect extends MobEffect {
    public StealthEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /**
     * Called when stealth is removed from an entity.
     * Spawns smoke particles and plays a sound to indicate stealth break.
     */
    public static void onRemove(LivingEntity entity) {
        if (!entity.level().isClientSide() && entity.level() instanceof ServerLevel serverLevel) {
            // Play stealth leave sound
            serverLevel.playSound(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.PLAYERS,
                    0.5f, 1.2f);

            // Spawn smoke particles
            for (int i = 0; i < 20; i++) {
                double dx = (entity.getRandom().nextDouble() - 0.5) * 0.8;
                double dy = entity.getRandom().nextDouble() * 1.2;
                double dz = (entity.getRandom().nextDouble() - 0.5) * 0.8;
                serverLevel.sendParticles(
                        ParticleTypes.POOF,
                        entity.getX() + dx, entity.getY() + dy, entity.getZ() + dz,
                        1, 0, 0, 0, 0.02);
            }
        }
    }
}
