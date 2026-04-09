package com.ultra.megamod.feature.relics.ability.hands_right;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class IronFistAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
        new RelicAbility("Heavy Blows", "Gain Strength when not holding a weapon",
            1, RelicAbility.CastType.PASSIVE,
            List.of(new RelicStat("strength_level", 0.0, 1.0, RelicStat.ScaleType.ADD, 0.15))),
        new RelicAbility("Uppercut", "Launch the nearest mob into the air with a powerful strike",
            4, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("damage", 5.0, 10.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.12),
                    new RelicStat("launch", 0.8, 1.5, RelicStat.ScaleType.ADD, 0.1))),
        new RelicAbility("Ground Pound", "Slam the ground to damage and slow all nearby mobs",
            7, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("damage", 6.0, 12.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                    new RelicStat("radius", 4.0, 6.0, RelicStat.ScaleType.ADD, 0.3)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Iron Fist", "Heavy Blows", IronFistAbility::executeHeavyBlows);
        AbilityCastHandler.registerAbility("Iron Fist", "Uppercut", IronFistAbility::executeUppercut);
        AbilityCastHandler.registerAbility("Iron Fist", "Ground Pound", IronFistAbility::executeGroundPound);
    }

    private static void executeHeavyBlows(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) return;
        ItemStack mainHand = player.getMainHandItem();
        boolean holdingWeapon = mainHand.is(ItemTags.SWORDS) || mainHand.getItem() instanceof AxeItem;
        if (!holdingWeapon) {
            int amplifier = Math.max(0, (int) stats[0]);
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, amplifier, false, true, true));
        }
    }

    private static void executeUppercut(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double launchPower = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(4.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= 4.0);
        // Find nearest target
        LivingEntity target = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity entity : targets) {
            double dist = entity.distanceTo((Entity) player);
            if (dist < closestDist) {
                closestDist = dist;
                target = entity;
            }
        }
        if (target != null) {
            target.hurt(level.damageSources().playerAttack((Player) player), damage);
            Vec3 motion = target.getDeltaMovement();
            target.setDeltaMovement(motion.x, launchPower, motion.z);
            target.hurtMarked = true;
            // Crit trail upward
            WeaponEffects.column(level, ParticleTypes.CRIT, target.getX(), target.getY(), target.getZ(),
                3.0, 8, 3, 0.15);
            level.sendParticles((ParticleOptions) ParticleTypes.CRIT,
                target.getX(), target.getY() + 0.5, target.getZ(), 10, 0.2, 0.3, 0.2, 0.2);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    private static void executeGroundPound(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);
        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().playerAttack((Player) player), damage);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, true, true));
            level.sendParticles((ParticleOptions) ParticleTypes.CAMPFIRE_COSY_SMOKE,
                target.getX(), target.getY() + 0.3, target.getZ(), 5, 0.2, 0.2, 0.2, 0.01);
        }
        // Ground shockwave
        WeaponEffects.shockwave(level, ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 0.2, player.getZ(),
            radius, 3, 18, 2);
        // Debris particles
        level.sendParticles((ParticleOptions) ParticleTypes.COMPOSTER,
            player.getX(), player.getY() + 0.3, player.getZ(), 15, radius * 0.4, 0.2, radius * 0.4, 0.02);
        // Impact dust cloud
        level.sendParticles((ParticleOptions) ParticleTypes.CAMPFIRE_COSY_SMOKE,
            player.getX(), player.getY() + 0.5, player.getZ(), 10, 0.5, 0.3, 0.5, 0.01);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0f, 0.7f);
    }
}
