package com.ultra.megamod.feature.combat.rogues.client.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;

/**
 * Demoralize effect particle spawner.
 * Ported from net.rogues.client.effect.DemoralizeParticles.
 *
 * Spawns dark smoke particles in a sphere around the entity's center,
 * colored with rage red, indicating the demoralized state.
 */
public class DemoralizeParticles {
    private final int particleCount;

    public DemoralizeParticles(int particleCount) {
        this.particleCount = particleCount;
    }

    /**
     * Spawns demoralize particles around the given entity.
     * Called each tick while the Demoralize effect is active.
     */
    public void spawnParticles(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) return;

        int count = particleCount * (amplifier + 1);
        for (int i = 0; i < count; i++) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 0.8;
            double y = entity.getY() + 0.5 + entity.getRandom().nextDouble() * 0.8;
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 0.8;
            entity.level().addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0.02, 0);
        }
    }
}
