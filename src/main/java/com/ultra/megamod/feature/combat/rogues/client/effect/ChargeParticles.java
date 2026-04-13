package com.ultra.megamod.feature.combat.rogues.client.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;

/**
 * Charge effect particle spawner.
 * Ported from net.rogues.client.effect.ChargeParticles.
 *
 * Spawns ascending flame-like stripe particles around the entity's feet
 * in a wide pipe shape, colored with rage red.
 */
public class ChargeParticles {
    private final int particleCount;

    public ChargeParticles(int particleCount) {
        this.particleCount = particleCount;
    }

    /**
     * Spawns charge particles around the given entity.
     * Called each tick while the Charge effect is active.
     */
    public void spawnParticles(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) return;

        int count = particleCount * (amplifier + 1);
        for (int i = 0; i < count; i++) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 0.6;
            double y = entity.getY() + entity.getRandom().nextDouble() * 0.4;
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 0.6;
            entity.level().addParticle(ParticleTypes.FLAME, x, y, z, 0, 0.06, 0);
        }
    }
}
