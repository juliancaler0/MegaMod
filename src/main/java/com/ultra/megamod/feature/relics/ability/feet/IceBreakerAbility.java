/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.AABB
 */
package com.ultra.megamod.feature.relics.ability.feet;

import com.ultra.megamod.feature.attributes.network.CombatTextSender;
import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
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

public class IceBreakerAbility {
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Stomp", "Freeze nearby entities with cold AOE", 1, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("radius", 3.0, 5.0, RelicStat.ScaleType.ADD, 0.3), new RelicStat("freeze_duration", 40.0, 100.0, RelicStat.ScaleType.ADD, 10.0))), new RelicAbility("Frost Thorns", "Chance to freeze attackers when hit", 4, RelicAbility.CastType.PASSIVE, List.of(new RelicStat("freeze_chance", 15.0, 35.0, RelicStat.ScaleType.ADD, 3.0))));

    public static void register() {
        AbilityCastHandler.registerAbility("Ice Breaker", "Stomp", IceBreakerAbility::executeStomp);
        AbilityCastHandler.registerAbility("Ice Breaker", "Frost Thorns", IceBreakerAbility::executeFrostThorns);
    }

    private static void executeStomp(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double radius = stats[0];
        int freezeDuration = (int)stats[1];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity != player && entity.isAlive() && (double)entity.distanceTo((Entity)player) <= radius);
        for (LivingEntity entity2 : entities) {
            entity2.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, freezeDuration, 3, false, true, true));
            entity2.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, freezeDuration, 1, false, true, true));
            entity2.setTicksFrozen(Math.max(entity2.getTicksFrozen(), freezeDuration));
        }
    }

    private static void executeFrostThorns(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
    }

    public static void executeFrostThornsOnHit(ServerPlayer wearer, LivingEntity attacker, double[] stats) {
        // 25% chance to proc
        if (wearer.getRandom().nextFloat() > 0.25f) return;

        ServerLevel level = (ServerLevel) wearer.level();
        int freezeDuration = stats.length > 1 ? (int) stats[1] : 60;

        // Freeze the attacker
        attacker.setTicksFrozen(Math.max(attacker.getTicksFrozen(), freezeDuration));
        attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, freezeDuration, 1, false, true));
        attacker.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, freezeDuration, 0, false, true));

        // Visual: snowflake burst around attacker
        level.sendParticles(ParticleTypes.SNOWFLAKE,
            attacker.getX(), attacker.getY() + attacker.getBbHeight() / 2, attacker.getZ(),
            15, 0.5, 0.5, 0.5, 0.02);

        // Ice crystal particles
        level.sendParticles(ParticleTypes.END_ROD,
            attacker.getX(), attacker.getY() + 0.5, attacker.getZ(),
            5, 0.3, 0.3, 0.3, 0.01);

        // Sound: glass crack
        level.playSound(null, attacker.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.7f, 1.5f);

        // Combat text
        CombatTextSender.sendEffect(attacker, "FROZEN!", 0xFF88CCFF);
    }
}

