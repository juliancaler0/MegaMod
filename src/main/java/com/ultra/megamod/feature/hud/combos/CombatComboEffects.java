package com.ultra.megamod.feature.hud.combos;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Applies the gameplay effects of each combat combo.
 */
public class CombatComboEffects {

    public static void applyCombo(ServerPlayer player, CombatComboRegistry.ComboDefinition combo) {
        ServerLevel level = (ServerLevel) player.level();

        switch (combo.id()) {
            case "shatter" -> {
                // AoE frost-fire burst: 8 damage to all mobs within 5 blocks
                AABB area = player.getBoundingBox().inflate(5.0);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player)) {
                    e.hurtServer(level, player.damageSources().magic(), 8.0f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, false, true));
                }
                level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 1, player.getZ(), 10, 2, 1, 2, 0.1);
            }
            case "counter_strike" -> {
                // 2x damage next hit (via Strength II for 3s)
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 2, false, true));
            }
            case "life_drain" -> {
                // Heal 50% of damage dealt for 8s (via Regeneration + Strength combo)
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1, false, true));
            }
            case "deep_freeze" -> {
                // Freeze nearby mobs 3s
                AABB area = player.getBoundingBox().inflate(6.0);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 5, false, true));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 2, false, true));
                }
                level.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1, player.getZ(), 30, 3, 1, 3, 0.05);
            }
            case "inferno" -> {
                // AoE fire damage over time
                AABB area = player.getBoundingBox().inflate(5.0);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player)) {
                    e.igniteForSeconds(5);
                    e.hurtServer(level, player.damageSources().magic(), 4.0f);
                }
                level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 40, 3, 1, 3, 0.05);
            }
            case "assassinate" -> {
                // Next hit ignores armor (Strength III for 3s as approximation)
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 3, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, true));
            }
            case "overcharge" -> {
                // +50% ability damage 5s (Strength II + Haste I)
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 1, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 100, 0, false, true));
            }
            case "sanctify" -> {
                // AoE heal to nearby players
                AABB area = player.getBoundingBox().inflate(8.0);
                for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, area)) {
                    p.heal(8.0f);
                    p.sendSystemMessage(Component.literal("Sanctified!").withStyle(ChatFormatting.GREEN));
                }
                level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1, player.getZ(), 15, 3, 1, 3, 0.05);
            }
            case "fortress" -> {
                // Damage immunity 2s
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 4, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 40, 3, false, true));
            }
            case "arcane_surge" -> {
                // +25% XP gain 10s (Luck + Haste as approximation)
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 200, 1, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 200, 0, false, true));
            }
        }

        // Common: play combo sound + particles
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.5f);
        level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1, player.getZ(), 20, 0.5, 0.5, 0.5, 0.5);
    }
}
