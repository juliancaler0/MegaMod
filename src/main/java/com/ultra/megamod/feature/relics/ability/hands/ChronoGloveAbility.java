package com.ultra.megamod.feature.relics.ability.hands;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import com.ultra.megamod.feature.relics.weapons.WeaponEffects;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class ChronoGloveAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Temporal Flux", "Grants Haste while equipped", 1,
                    RelicAbility.CastType.PASSIVE, List.of(
                    new RelicStat("level", 0.0, 1.0, RelicStat.ScaleType.ADD, 0.15))),
            new RelicAbility("Time Snap", "Teleport backward and heal", 5,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("heal", 3.0, 6.0, RelicStat.ScaleType.ADD, 0.4))),
            new RelicAbility("Stasis Field", "Freeze all enemies in radius", 8,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("radius", 4.0, 6.0, RelicStat.ScaleType.ADD, 0.3),
                    new RelicStat("duration", 60.0, 120.0, RelicStat.ScaleType.ADD, 10.0)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Chrono Glove", "Temporal Flux", ChronoGloveAbility::executeTemporalFlux);
        AbilityCastHandler.registerAbility("Chrono Glove", "Time Snap", ChronoGloveAbility::executeTimeSnap);
        AbilityCastHandler.registerAbility("Chrono Glove", "Stasis Field", ChronoGloveAbility::executeStasisField);
    }

    private static void executeTemporalFlux(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) {
            return;
        }
        int amplifier = (int) stats[0];
        player.addEffect(new MobEffectInstance(MobEffects.HASTE, 40, amplifier, false, true, true));
        ServerLevel level = (ServerLevel) player.level();
        if (player.tickCount % 40 == 0) {
            level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.3, 0.5, 0.3, 0.5);
        }
    }

    private static void executeTimeSnap(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float heal = (float) stats[0];
        ServerLevel level = (ServerLevel) player.level();

        // Store original position for particles
        double origX = player.getX();
        double origY = player.getY();
        double origZ = player.getZ();

        // Teleport 5 blocks backward along reverse look direction
        Vec3 look = player.getLookAngle();
        double newX = origX - look.x * 5.0;
        double newY = origY;
        double newZ = origZ - look.z * 5.0;

        player.teleportTo(newX, newY, newZ);
        player.heal(heal);

        // Particles at departure point
        level.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL,
                origX, origY + 1.0, origZ, 20, 0.5, 0.8, 0.5, 0.05);
        level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT,
                origX, origY + 1.0, origZ, 10, 0.3, 0.5, 0.3, 0.5);

        // Particles at arrival point
        level.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL,
                newX, newY + 1.0, newZ, 20, 0.5, 0.8, 0.5, 0.05);
        level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT,
                newX, newY + 1.0, newZ, 10, 0.3, 0.5, 0.3, 0.5);

        level.playSound(null, newX, newY, newZ,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.5f);
    }

    private static void executeStasisField(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double radius = stats[0];
        int duration = (int) stats[1];
        ServerLevel level = (ServerLevel) player.level();
        double cx = player.getX();
        double cy = player.getY();
        double cz = player.getZ();

        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);

        for (LivingEntity entity : entities) {
            entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 4, false, true, true));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 3, false, true, true));
            entity.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, duration, 2, false, true, true));
        }

        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.ENCHANT, cx, cy + 0.3, cz, radius, 24, 2, 0.02);
        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.END_ROD, cx, cy + 1.0, cz, radius, 20, 1, 0.02);
        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.ENCHANT, cx, cy + 1.8, cz, radius, 24, 2, 0.02);
        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.END_ROD, cx, cy + 2.5, cz, radius, 20, 1, 0.02);

        level.playSound(null, cx, cy, cz,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 2.0f, 0.5f);
    }
}
