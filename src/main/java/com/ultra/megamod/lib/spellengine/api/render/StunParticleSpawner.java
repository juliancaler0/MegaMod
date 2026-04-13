package com.ultra.megamod.lib.spellengine.api.render;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import com.ultra.megamod.lib.spellengine.api.effect.CustomParticleStatusEffect;

public class StunParticleSpawner implements CustomParticleStatusEffect.Spawner {

    private final ParticleOptions particleEffect;
    public StunParticleSpawner() {
        this.particleEffect = ParticleTypes.CRIT;
    }

    public StunParticleSpawner(Identifier particleId) {
        var particleOpt = BuiltInRegistries.PARTICLE_TYPE.get(particleId);
        if (particleOpt.isEmpty()) {
            throw new IllegalArgumentException("Particle type not found: " + particleId);
        }
        this.particleEffect = (ParticleOptions) particleOpt.get().value();
    }

    @Override
    public void spawnParticles(LivingEntity livingEntity, int amplifier) {
        var world = livingEntity.level();
        if (world.isClientSide() && world instanceof ClientLevel clientWorld) {
            var time = livingEntity.tickCount; // Offset by age so mobs don't look exactly same next to each other
            var angle = Math.toRadians((time % 360) * 18F); // 18 degree per tick = 360 per sec
            var rotated = new Vec3(0, 0, livingEntity.getBbWidth() * 0.5F).yRot((float) angle);
            var spawnPosition = livingEntity
                    .position()
                    .add(0, livingEntity.getBbHeight() * 1.2F, 0)
                    .add(rotated);
            // System.out.println("Spawning stun particle at angle: " + (angle % 360) + " time: " + time);
            clientWorld.addParticle(particleEffect, spawnPosition.x, spawnPosition.y, spawnPosition.z, 0.0, 0.0, 0.0);
        }
    }
}
