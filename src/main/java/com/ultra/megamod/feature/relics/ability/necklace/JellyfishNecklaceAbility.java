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
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.ultra.megamod.feature.relics.ability.necklace;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.Comparator;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class JellyfishNecklaceAbility {
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Unsinkable", "Float automatically in water", 1, RelicAbility.CastType.PASSIVE, List.of()), new RelicAbility("Shock", "Electric AOE that damages and stuns", 3, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("damage", 3.0, 8.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1), new RelicStat("radius", 3.0, 5.0, RelicStat.ScaleType.ADD, 0.3))), new RelicAbility("Paralysis", "Stun a single target with electricity", 6, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("duration", 2.0, 5.0, RelicStat.ScaleType.ADD, 0.5))));

    public static void register() {
        AbilityCastHandler.registerAbility("Jellyfish Necklace", "Unsinkable", JellyfishNecklaceAbility::executeUnsinkable);
        AbilityCastHandler.registerAbility("Jellyfish Necklace", "Shock", JellyfishNecklaceAbility::executeShock);
        AbilityCastHandler.registerAbility("Jellyfish Necklace", "Paralysis", JellyfishNecklaceAbility::executeParalysis);
    }

    private static void executeUnsinkable(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (!player.isInWater()) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        if (motion.y < 0.0 && !player.isShiftKeyDown()) {
            player.setDeltaMovement(motion.x, motion.y + 0.06, motion.z);
            player.hurtMarked = true;
        }
    }

    private static void executeShock(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double damage = stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        for (LivingEntity entity : entities) {
            entity.hurt(player.damageSources().magic(), (float)damage);
            entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, true, true));
        }
    }

    private static void executeParalysis(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        AABB area;
        double durationSeconds = stats[0];
        int durationTicks = (int)(durationSeconds * 20.0);
        ServerLevel level = (ServerLevel) player.level();
        List<Monster> hostiles = level.getEntitiesOfClass(Monster.class, area = new AABB(player.blockPosition()).inflate(8.0), e -> e.isAlive());
        Monster nearest = hostiles.stream().min(Comparator.comparingDouble(e -> e.distanceTo((Entity)player))).orElse(null);
        if (nearest != null) {
            nearest.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, durationTicks, 4, false, true, true));
            nearest.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, durationTicks, 4, false, true, true));
            nearest.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, durationTicks, 1, false, true, true));
        }
    }
}

