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
import java.util.Comparator;
import java.util.List;

public class ThornweaveGloveAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Thorned Grasp", "Reflect damage to nearby attackers", 1,
                    RelicAbility.CastType.PASSIVE, List.of(
                    new RelicStat("reflect_damage", 1.0, 3.0, RelicStat.ScaleType.ADD, 0.3))),
            new RelicAbility("Vine Lash", "Pull a mob toward you with damaging vines", 4,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("damage", 3.0, 6.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                    new RelicStat("range", 6.0, 10.0, RelicStat.ScaleType.ADD, 0.5))),
            new RelicAbility("Entangle", "Root all enemies in radius with vines", 7,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("radius", 4.0, 6.0, RelicStat.ScaleType.ADD, 0.3),
                    new RelicStat("duration", 60.0, 100.0, RelicStat.ScaleType.ADD, 5.0)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Thornweave Glove", "Thorned Grasp", ThornweaveGloveAbility::executeThornedGrasp);
        AbilityCastHandler.registerAbility("Thornweave Glove", "Vine Lash", ThornweaveGloveAbility::executeVineLash);
        AbilityCastHandler.registerAbility("Thornweave Glove", "Entangle", ThornweaveGloveAbility::executeEntangle);
    }

    private static void executeThornedGrasp(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 40 != 0) {
            return;
        }
        float reflectDamage = (float) stats[0];
        if (player.getLastHurtByMob() == null) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(3.0);
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());
        if (nearby.isEmpty()) {
            return;
        }
        nearby.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) player)));
        LivingEntity target = nearby.getFirst();
        target.hurt(level.damageSources().thorns(player), reflectDamage);
        level.sendParticles((ParticleOptions) ParticleTypes.COMPOSTER,
                target.getX(), target.getY() + (double) target.getBbHeight() / 2.0, target.getZ(),
                6, 0.3, 0.3, 0.3, 0.01);
    }

    private static void executeVineLash(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double range = stats[1];
        ServerLevel level = (ServerLevel) player.level();

        AABB searchArea = new AABB(player.blockPosition()).inflate(range);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, searchArea,
                e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= range);
        if (targets.isEmpty()) {
            return;
        }
        targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) player)));
        LivingEntity target = targets.getFirst();

        target.hurt(level.damageSources().magic(), damage);

        // Pull target toward player
        double dx = player.getX() - target.getX();
        double dy = player.getY() - target.getY();
        double dz = player.getZ() - target.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist > 0.5) {
            target.setDeltaMovement(target.getDeltaMovement().add(
                    dx / dist * 1.2, dy / dist * 0.5 + 0.2, dz / dist * 1.2));
            target.hurtMarked = true;
        }

        Vec3 targetPos = target.position().add(0.0, (double) target.getBbHeight() / 2.0, 0.0);
        Vec3 playerPos = player.getEyePosition();
        WeaponEffects.line(level, (ParticleOptions) ParticleTypes.COMPOSTER, targetPos, playerPos, 15, 2, 0.08);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.7f);
    }

    private static void executeEntangle(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double radius = stats[0];
        int duration = (int) stats[1];
        ServerLevel level = (ServerLevel) player.level();

        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);

        for (LivingEntity entity : entities) {
            entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 4, false, true, true));
            entity.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, duration, 1, false, true, true));

            WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.COMPOSTER,
                    entity.getX(), entity.getY() + 0.1, entity.getZ(), 0.6, 8, 2, 0.02);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.5f, 0.6f);
    }
}
