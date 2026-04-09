/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.phys.Vec3
 */
package com.ultra.megamod.feature.dungeons.boss;

import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class BossPhaseManager {
    private BossPhaseManager() {
    }

    public static void checkPhaseTransition(DungeonBossEntity boss) {
        if (boss.level().isClientSide()) {
            return;
        }
        if (!boss.isAlive()) {
            return;
        }
        float healthPercent = boss.getHealth() / boss.getMaxHealth();
        int maxPhases = boss.getMaxPhases();
        int newPhase = maxPhases >= 4 && healthPercent <= 0.25f ? 4 : (maxPhases >= 3 && healthPercent <= 0.5f ? 3 : (maxPhases >= 2 && healthPercent <= 0.75f ? 2 : 1));
        if (newPhase > boss.getCurrentPhase()) {
            boss.setPhase(newPhase);
        }
    }

    public static void spawnPhaseParticles(ServerLevel level, Vec3 pos) {
        level.sendParticles((ParticleOptions)ParticleTypes.CLOUD, pos.x, pos.y + 1.0, pos.z, 15, 1.0, 1.0, 1.0, 0.1);
        for (int i = 0; i < 30; ++i) {
            double angle = Math.PI * 2 * (double)i / 30.0;
            double px = pos.x + Math.cos(angle) * 3.0;
            double pz = pos.z + Math.sin(angle) * 3.0;
            level.sendParticles((ParticleOptions)ParticleTypes.SOUL_FIRE_FLAME, px, pos.y + 0.5, pz, 1, 0.0, 0.3, 0.0, 0.02);
        }
        level.sendParticles((ParticleOptions)ParticleTypes.END_ROD, pos.x, pos.y + 1.5, pos.z, 20, 1.5, 1.0, 1.5, 0.05);
    }
}

