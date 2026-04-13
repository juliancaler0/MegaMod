package com.ultra.megamod.feature.combat.rogues.client.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;

/**
 * Shatter effect particle spawner.
 * Ported from net.rogues.client.effect.ShatterParticles.
 *
 * Spawns dripping blood-like particles in a sphere around the entity,
 * indicating shattered armor.
 */
public class ShatterParticles {
    private final int particleCount;

    public ShatterParticles(int particleCount) {
        this.particleCount = particleCount;
    }

    /**
     * Spawns shatter particles around the given entity.
     * Called each tick while the Shatter effect is active.
     */
    public void spawnParticles(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) return;

        int count = particleCount * (amplifier + 1);
        for (int i = 0; i < count; i++) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 0.8;
            double y = entity.getY() + 0.5 + entity.getRandom().nextDouble() * 1.0;
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 0.8;
            entity.level().addParticle(ParticleTypes.DRIPPING_LAVA, x, y, z, 0, -0.05, 0);
        }
    }
}
