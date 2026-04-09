package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Handles all experience related things of the MC citizen.
 * Ported from MineColonies' CitizenExperienceHandler.
 */
public class CitizenExperienceHandler implements ICitizenExperienceHandler {

    private static final int PRIMARY_DEPENDENCY_SHARE = 10;
    private static final int SECONDARY_DEPENDENCY_SHARE = 5;
    private static final int MAX_XP_PICKUP_ATTEMPTS = 5;
    private static final int XP_PARTICLE_EXPLOSION_SIZE = 20;

    private final MCEntityCitizen citizen;
    private int counterMovedXp = 0;

    public CitizenExperienceHandler(MCEntityCitizen citizen) {
        this.citizen = citizen;
    }

    @Override
    public void updateLevel() {
        // Level updates handled through citizen data
    }

    @Override
    public void addExperience(double xp) {
        // XP integration point: pipe xp into existing MegaMod citizen skill system
        // For now, store as raw XP on the citizen entity
        citizen.addRawXp(xp);
    }

    @Override
    public void dropExperience() {
        if (citizen.level().isClientSide()) {
            return;
        }

        if (citizen.getLastHurtByPlayerTime() > 0 && (citizen.tickCount - citizen.getLastHurtByPlayerTime()) < 100) {
            int experience = (int) citizen.getTotalXp();
            while (experience > 0) {
                int j = ExperienceOrb.getExperienceValue(experience);
                experience -= j;
                citizen.level().addFreshEntity(
                        new ExperienceOrb(citizen.level(), citizen.getX(), citizen.getY(), citizen.getZ(), j));
            }
        }

        // Spawn particle explosion on death
        for (int i = 0; i < XP_PARTICLE_EXPLOSION_SIZE; ++i) {
            double d2 = citizen.getRandom().nextGaussian() * 0.02D;
            double d0 = citizen.getRandom().nextGaussian() * 0.02D;
            double d1 = citizen.getRandom().nextGaussian() * 0.02D;
            citizen.level().addParticle(ParticleTypes.EXPLOSION,
                    citizen.getX() + (citizen.getRandom().nextDouble() * citizen.getBbWidth() * 2.0F) - citizen.getBbWidth(),
                    citizen.getY() + (citizen.getRandom().nextDouble() * citizen.getBbHeight()),
                    citizen.getZ() + (citizen.getRandom().nextDouble() * citizen.getBbWidth() * 2.0F) - citizen.getBbWidth(),
                    d2, d0, d1);
        }
    }

    @Override
    public void gatherXp() {
        if (citizen.level().isClientSide()) {
            return;
        }

        int growSize = counterMovedXp > 0 || citizen.getRandom().nextInt(100) < 20 ? 4 : 2;
        final AABB box = citizen.getBoundingBox().inflate(growSize);

        boolean movedXp = false;

        for (@NotNull ExperienceOrb orb : citizen.level().getEntitiesOfClass(ExperienceOrb.class, box)) {
            if (orb.tickCount < 5) {
                continue;
            }

            Vec3 vec3d = new Vec3(
                    citizen.getX() - orb.getX(),
                    citizen.getY() + citizen.getEyeHeight() / 2.0D - orb.getY(),
                    citizen.getZ() - orb.getZ());
            double d1 = vec3d.lengthSqr();

            if (d1 < 1.0D) {
                addExperience(orb.getValue());
                orb.remove(Entity.RemovalReason.DISCARDED);
                counterMovedXp = 0;
            } else if (counterMovedXp > MAX_XP_PICKUP_ATTEMPTS) {
                addExperience(orb.getValue());
                orb.remove(Entity.RemovalReason.DISCARDED);
                counterMovedXp = 0;
                return;
            }

            double d2 = 1.0D - Math.sqrt(d1) / 8.0D;
            orb.setDeltaMovement(orb.getDeltaMovement().add(vec3d.normalize().scale(d2 * d2 * 0.1D)));
            movedXp = true;
            counterMovedXp++;
        }
        if (!movedXp) {
            counterMovedXp = 0;
        }
    }
}
