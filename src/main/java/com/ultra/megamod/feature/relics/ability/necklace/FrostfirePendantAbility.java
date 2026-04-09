package com.ultra.megamod.feature.relics.ability.necklace;

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

public class FrostfirePendantAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Equilibrium", "Fire Resistance in hot biomes, Resistance in cold biomes", 1,
                    RelicAbility.CastType.PASSIVE, List.of()),
            new RelicAbility("Frostfire Bolt", "Fire a dual-element bolt at the nearest mob", 4,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("damage", 5.0, 10.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1))),
            new RelicAbility("Elemental Storm", "Fire and ice AOE storm", 7,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("damage", 6.0, 12.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.12),
                    new RelicStat("radius", 4.0, 7.0, RelicStat.ScaleType.ADD, 0.5)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Frostfire Pendant", "Equilibrium", FrostfirePendantAbility::executeEquilibrium);
        AbilityCastHandler.registerAbility("Frostfire Pendant", "Frostfire Bolt", FrostfirePendantAbility::executeFrostfireBolt);
        AbilityCastHandler.registerAbility("Frostfire Pendant", "Elemental Storm", FrostfirePendantAbility::executeElementalStorm);
    }

    private static void executeEquilibrium(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 100 != 0) {
            return;
        }
        float temperature = player.level().getBiome(player.blockPosition()).value().getBaseTemperature();
        if (temperature > 1.0f) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 120, 0, false, true, true));
            ServerLevel level = (ServerLevel) player.level();
            level.sendParticles((ParticleOptions) ParticleTypes.FLAME,
                    player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.3, 0.5, 0.3, 0.01);
        } else if (temperature < 0.2f) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 120, 0, false, true, true));
            ServerLevel level = (ServerLevel) player.level();
            level.sendParticles((ParticleOptions) ParticleTypes.SNOWFLAKE,
                    player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.3, 0.5, 0.3, 0.01);
        }
    }

    private static void executeFrostfireBolt(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB searchArea = new AABB(player.blockPosition()).inflate(16.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, searchArea,
                e -> e != player && e.isAlive());
        if (targets.isEmpty()) {
            return;
        }
        targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) player)));
        LivingEntity target = targets.getFirst();

        float fireDamage = damage / 2.0f;
        float magicDamage = damage / 2.0f;
        target.setRemainingFireTicks(60);
        target.hurt(level.damageSources().onFire(), fireDamage);
        target.hurt(level.damageSources().magic(), magicDamage);

        Vec3 start = player.getEyePosition();
        Vec3 end = target.position().add(0.0, (double) target.getBbHeight() / 2.0, 0.0);
        WeaponEffects.line(level, (ParticleOptions) ParticleTypes.FLAME, start, end, 15, 1, 0.05);
        WeaponEffects.line(level, (ParticleOptions) ParticleTypes.SNOWFLAKE, start, end, 15, 1, 0.08);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void executeElementalStorm(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);

        for (LivingEntity entity : entities) {
            entity.hurt(level.damageSources().magic(), damage);
            entity.setRemainingFireTicks(40);
        }

        double cx = player.getX();
        double cy = player.getY() + 0.2;
        double cz = player.getZ();
        WeaponEffects.shockwave(level, (ParticleOptions) ParticleTypes.FLAME, cx, cy, cz,
                radius * 0.5, 2, 16, 1);
        WeaponEffects.shockwave(level, (ParticleOptions) ParticleTypes.SNOWFLAKE, cx, cy, cz,
                radius, 2, 16, 1);
        level.playSound(null, cx, cy, cz,
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5f, 0.8f);
    }
}
