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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ArcaneGauntletAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
        new RelicAbility("Mana Shield", "Generate an absorption shield when you have enough XP",
            1, RelicAbility.CastType.PASSIVE,
            List.of(new RelicStat("absorption", 1.0, 3.0, RelicStat.ScaleType.ADD, 0.3))),
        new RelicAbility("Arcane Bolt", "Fire a magic bolt at the nearest mob",
            3, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("damage", 4.0, 8.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1))),
        new RelicAbility("Spellweave", "Empower yourself with Strength and Haste",
            7, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("duration", 100.0, 200.0, RelicStat.ScaleType.ADD, 15.0)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Arcane Gauntlet", "Mana Shield", ArcaneGauntletAbility::executeManaShield);
        AbilityCastHandler.registerAbility("Arcane Gauntlet", "Arcane Bolt", ArcaneGauntletAbility::executeArcaneBolt);
        AbilityCastHandler.registerAbility("Arcane Gauntlet", "Spellweave", ArcaneGauntletAbility::executeSpellweave);
    }

    private static void executeManaShield(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 40 != 0) return;
        if (player.experienceLevel < 10) return;
        int amplifier = Math.max(0, (int) stats[0] - 1);
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60, amplifier, false, true, true));
        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT,
            player.getX(), player.getY() + 1.0, player.getZ(), 4, 0.3, 0.5, 0.3, 0.3);
    }

    private static void executeArcaneBolt(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(10.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= 10.0);
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
            target.hurt(level.damageSources().magic(), damage);
            // Arcane bolt line to target
            Vec3 from = player.getEyePosition();
            Vec3 to = target.position().add(0, target.getBbHeight() * 0.5, 0);
            WeaponEffects.line(level, ParticleTypes.ENCHANT, from, to, 14, 2, 0.05);
            WeaponEffects.line(level, ParticleTypes.END_ROD, from, to, 8, 1, 0.03);
            // Impact burst
            level.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT,
                target.getX(), target.getY() + 0.5, target.getZ(), 12, 0.3, 0.4, 0.3, 0.1);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.SHULKER_SHOOT, SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    private static void executeSpellweave(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        int duration = (int) stats[0];
        ServerLevel level = (ServerLevel) player.level();
        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, duration, 0, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.HASTE, duration, 0, false, true, true));
        // Spiral of enchant particles
        WeaponEffects.spiral(level, ParticleTypes.ENCHANT, player.getX(), player.getY(), player.getZ(),
            1.5, 2.5, 24, 2);
        // END_ROD burst
        level.sendParticles((ParticleOptions) ParticleTypes.END_ROD,
            player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.5, 0.8, 0.5, 0.08);
        // Enchant sparkle aura
        level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT,
            player.getX(), player.getY() + 0.5, player.getZ(), 20, 1.0, 1.0, 1.0, 0.5);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
