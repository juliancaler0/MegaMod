package com.ultra.megamod.lib.spellengine.api.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;

import java.util.HashMap;
import java.util.Map;

public class CustomParticleStatusEffect {
    private static final Map<MobEffect, Spawner> spawners = new HashMap<>();

    public static void register(MobEffect statusEffect, Spawner spawner) {
        spawners.put(statusEffect, spawner);
    }

    public static Spawner spawnerOf(MobEffect statusEffect) {
        return spawners.get(statusEffect);
    }

    public interface Spawner {
        void spawnParticles(LivingEntity livingEntity, int amplifier);
    }
}
