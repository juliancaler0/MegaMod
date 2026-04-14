package com.ultra.megamod.feature.relics.ability.usable;

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

public class ThunderhornAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("War Cry", "Buff nearby allies with Strength and Speed", 1,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("radius", 6.0, 10.0, RelicStat.ScaleType.ADD, 0.5),
                    new RelicStat("duration", 160.0, 240.0, RelicStat.ScaleType.ADD, 10.0))),
            new RelicAbility("Sonic Boom", "Cone knockback and damage", 5,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("damage", 5.0, 10.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                    new RelicStat("range", 5.0, 8.0, RelicStat.ScaleType.ADD, 0.5)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Thunderhorn", "War Cry", ThunderhornAbility::executeWarCry);
        AbilityCastHandler.registerAbility("Thunderhorn", "Sonic Boom", ThunderhornAbility::executeSonicBoom);
    }

    private static void executeWarCry(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double radius = stats[0];
        int duration = (int) stats[1];
        ServerLevel level = (ServerLevel) player.level();

        // Buff self
        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, duration, 0, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, duration, 0, false, true, true));

        // Buff nearby players
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<ServerPlayer> allies = level.getEntitiesOfClass(ServerPlayer.class, area,
                p -> p != player && p.isAlive() && (double) p.distanceTo((Entity) player) <= radius);

        for (ServerPlayer ally : allies) {
            ally.addEffect(new MobEffectInstance(MobEffects.STRENGTH, duration, 0, false, true, true));
            ally.addEffect(new MobEffectInstance(MobEffects.SPEED, duration, 0, false, true, true));
        }

        double cx = player.getX();
        double cy = player.getY();
        double cz = player.getZ();
        WeaponEffects.shockwave(level, (ParticleOptions) ParticleTypes.ELECTRIC_SPARK,
                cx, cy + 0.5, cz, radius, 3, 20, 2);
        WeaponEffects.shockwave(level, (ParticleOptions) ParticleTypes.END_ROD,
                cx, cy + 1.0, cz, radius * 0.6, 2, 12, 1);

        // Spawn a damage+knockback shockwave ring entity that expands outward
        com.ultra.megamod.feature.relics.entity.ShockwaveEntity wave =
            new com.ultra.megamod.feature.relics.entity.ShockwaveEntity(
                level, cx, cy, cz, player.getId(), 4.0F, (float) radius);
        level.addFreshEntity(wave);

        level.playSound(null, cx, cy, cz,
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 2.0f, 0.6f);
    }

    private static void executeSonicBoom(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double range = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        Vec3 look = player.getLookAngle();
        double cx = player.getX();
        double cy = player.getY() + 1.0;
        double cz = player.getZ();

        AABB searchArea = new AABB(player.blockPosition()).inflate(range);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchArea,
                e -> e != player && e.isAlive());

        for (LivingEntity entity : entities) {
            Vec3 toEntity = entity.position().subtract(player.position()).normalize();
            double dot = look.x * toEntity.x + look.z * toEntity.z;
            double dist = entity.distanceTo((Entity) player);
            if (dot > 0.5 && dist <= range) {
                entity.hurt(level.damageSources().magic(), damage);
                entity.knockback(3.0, -look.x, -look.z);
            }
        }

        WeaponEffects.arc(level, (ParticleOptions) ParticleTypes.CLOUD,
                cx, cy, cz, look.x, look.z, range, Math.PI / 2.0, 20, 2);
        WeaponEffects.arc(level, (ParticleOptions) ParticleTypes.CRIT,
                cx, cy, cz, look.x, look.z, range * 0.7, Math.PI / 2.0, 15, 1);

        level.playSound(null, cx, cy, cz,
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 2.0f, 0.5f);
    }
}
