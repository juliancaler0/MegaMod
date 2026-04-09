package com.ultra.megamod.feature.relics.ability.ring;

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
import java.util.Comparator;
import java.util.List;

public class EmberstoneBandAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Warm Aura", "Passive Fire Resistance and freeze immunity", 1,
                    RelicAbility.CastType.PASSIVE, List.of()),
            new RelicAbility("Fire Snap", "Ignite the nearest mob with fire damage", 3,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("damage", 3.0, 6.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1))),
            new RelicAbility("Combustion", "Detonate all burning mobs in radius", 6,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("damage", 6.0, 12.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.12),
                    new RelicStat("radius", 5.0, 8.0, RelicStat.ScaleType.ADD, 0.5)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Emberstone Band", "Warm Aura", EmberstoneBandAbility::executeWarmAura);
        AbilityCastHandler.registerAbility("Emberstone Band", "Fire Snap", EmberstoneBandAbility::executeFireSnap);
        AbilityCastHandler.registerAbility("Emberstone Band", "Combustion", EmberstoneBandAbility::executeCombustion);
    }

    private static void executeWarmAura(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 100 != 0) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 120, 0, false, true, true));
        player.setTicksFrozen(0);
        ServerLevel level = (ServerLevel) player.level();
        if (player.tickCount % 200 == 0) {
            level.sendParticles((ParticleOptions) ParticleTypes.FLAME,
                    player.getX(), player.getY() + 0.8, player.getZ(), 2, 0.2, 0.3, 0.2, 0.005);
        }
    }

    private static void executeFireSnap(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
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

        target.hurt(level.damageSources().magic(), damage);
        target.setRemainingFireTicks(100);

        double tx = target.getX();
        double ty = target.getY() + (double) target.getBbHeight() / 2.0;
        double tz = target.getZ();
        level.sendParticles((ParticleOptions) ParticleTypes.FLAME,
                tx, ty, tz, 15, 0.4, 0.4, 0.4, 0.02);
        level.sendParticles((ParticleOptions) ParticleTypes.LAVA,
                tx, ty, tz, 5, 0.3, 0.3, 0.3, 0.0);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    private static void executeCombustion(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();

        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);

        for (LivingEntity entity : entities) {
            if (entity.getRemainingFireTicks() > 0) {
                // Burning mobs take bonus damage
                entity.hurt(level.damageSources().magic(), damage * 1.5f);
                WeaponEffects.shockwave(level, (ParticleOptions) ParticleTypes.FLAME,
                        entity.getX(), entity.getY() + 0.5, entity.getZ(), 1.5, 2, 8, 2);
            } else {
                entity.hurt(level.damageSources().magic(), damage);
                entity.setRemainingFireTicks(60);
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5f, 1.0f);
    }
}
