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

public class BloodstoneChokerAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Bloodlust", "Regenerate when health is low", 1,
                    RelicAbility.CastType.PASSIVE, List.of(
                    new RelicStat("threshold", 40.0, 60.0, RelicStat.ScaleType.ADD, 3.0))),
            new RelicAbility("Blood Price", "Sacrifice HP for high damage to nearest mob", 4,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("damage", 8.0, 16.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.12))),
            new RelicAbility("Sanguine Feast", "Drain life from all nearby enemies", 7,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("damage", 3.0, 6.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                    new RelicStat("radius", 4.0, 6.0, RelicStat.ScaleType.ADD, 0.3),
                    new RelicStat("heal_per_mob", 1.0, 3.0, RelicStat.ScaleType.ADD, 0.3)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Bloodstone Choker", "Bloodlust", BloodstoneChokerAbility::executeBloodlust);
        AbilityCastHandler.registerAbility("Bloodstone Choker", "Blood Price", BloodstoneChokerAbility::executeBloodPrice);
        AbilityCastHandler.registerAbility("Bloodstone Choker", "Sanguine Feast", BloodstoneChokerAbility::executeSanguineFeast);
    }

    private static void executeBloodlust(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) {
            return;
        }
        double thresholdPercent = stats[0];
        float maxHealth = player.getMaxHealth();
        float threshold = maxHealth * (float) (thresholdPercent / 100.0);
        if (player.getHealth() < threshold) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, true, true));
            ServerLevel level = (ServerLevel) player.level();
            level.sendParticles((ParticleOptions) ParticleTypes.CRIMSON_SPORE,
                    player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.3, 0.5, 0.3, 0.01);
        }
    }

    private static void executeBloodPrice(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        ServerLevel level = (ServerLevel) player.level();

        // Sacrifice 4 HP
        player.hurt(level.damageSources().magic(), 4.0f);

        AABB searchArea = new AABB(player.blockPosition()).inflate(16.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, searchArea,
                e -> e != player && e.isAlive());
        if (targets.isEmpty()) {
            return;
        }
        targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) player)));
        LivingEntity target = targets.getFirst();

        target.hurt(level.damageSources().magic(), damage);

        Vec3 start = player.getEyePosition();
        Vec3 end = target.position().add(0.0, (double) target.getBbHeight() / 2.0, 0.0);
        WeaponEffects.line(level, (ParticleOptions) ParticleTypes.DAMAGE_INDICATOR, start, end, 15, 1, 0.05);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    private static void executeSanguineFeast(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double radius = stats[1];
        float healPerMob = (float) stats[2];
        ServerLevel level = (ServerLevel) player.level();
        double cx = player.getX();
        double cy = player.getY();
        double cz = player.getZ();

        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);

        int mobsHit = 0;
        for (LivingEntity entity : entities) {
            entity.hurt(level.damageSources().magic(), damage);
            mobsHit++;

            Vec3 mobPos = entity.position().add(0.0, (double) entity.getBbHeight() / 2.0, 0.0);
            Vec3 playerPos = new Vec3(cx, cy + 1.0, cz);
            WeaponEffects.converge(level, (ParticleOptions) ParticleTypes.DAMAGE_INDICATOR,
                    mobPos.x, mobPos.y, mobPos.z, 0.5, 6, 1);
            WeaponEffects.line(level, (ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME,
                    mobPos, playerPos, 8, 1, 0.05);
        }

        if (mobsHit > 0) {
            player.heal(healPerMob * mobsHit);
            level.sendParticles((ParticleOptions) ParticleTypes.HEART,
                    cx, cy + 2.0, cz, mobsHit, 0.5, 0.3, 0.5, 0.0);
        }

        WeaponEffects.converge(level, (ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME,
                cx, cy + 1.0, cz, radius, 16, 2);
        level.playSound(null, cx, cy, cz,
                SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0f, 1.2f);
    }
}
