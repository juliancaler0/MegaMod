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
 */
package com.ultra.megamod.feature.relics.ability.belt;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class HunterBeltAbility {
    private static final Map<UUID, UUID> MARKED_TARGETS = new ConcurrentHashMap<UUID, UUID>();
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Tracker", "Applies Glowing to nearest hostile mob", 1, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("range", 20.0, 40.0, RelicStat.ScaleType.ADD, 3.0), new RelicStat("duration", 8.0, 15.0, RelicStat.ScaleType.ADD, 1.0))), new RelicAbility("Harvest", "Increases mob loot drops", 4, RelicAbility.CastType.PASSIVE, List.of(new RelicStat("loot_bonus", 0.1, 0.3, RelicStat.ScaleType.ADD, 0.03))), new RelicAbility("Predator", "Mark a target for bonus damage", 6, RelicAbility.CastType.TOGGLE, List.of(new RelicStat("damage_bonus", 15.0, 35.0, RelicStat.ScaleType.ADD, 3.0))));

    public static void register() {
        AbilityCastHandler.registerAbility("Hunter Belt", "Tracker", HunterBeltAbility::executeTracker);
        AbilityCastHandler.registerAbility("Hunter Belt", "Harvest", HunterBeltAbility::executeHarvest);
        AbilityCastHandler.registerAbility("Hunter Belt", "Predator", HunterBeltAbility::executePredator);
    }

    public static UUID getMarkedTarget(UUID playerId) {
        return MARKED_TARGETS.get(playerId);
    }

    public static void clearMarkedTarget(UUID playerId) {
        MARKED_TARGETS.remove(playerId);
    }

    public static boolean isMarkedTarget(ServerPlayer player, LivingEntity target) {
        UUID markedId = MARKED_TARGETS.get(player.getUUID());
        return markedId != null && markedId.equals(target.getUUID());
    }

    private static void executeHarvest(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 200 != 0) {
            return;
        }
        double lootBonus = stats[0];
        int level = lootBonus >= 0.2 ? 1 : 0;
        player.addEffect(new MobEffectInstance(MobEffects.LUCK, 210, level, false, false, true));
    }

    private static void executeTracker(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        AABB area;
        double range = stats[0];
        double durationSeconds = stats[1];
        int durationTicks = (int)(durationSeconds * 20.0);
        ServerLevel level = (ServerLevel) player.level();
        List<Monster> hostiles = level.getEntitiesOfClass(Monster.class, area = new AABB(player.blockPosition()).inflate(range), e -> e.isAlive());
        Monster nearest = hostiles.stream().min(Comparator.comparingDouble(e -> e.distanceTo((Entity)player))).orElse(null);
        if (nearest != null) {
            nearest.addEffect(new MobEffectInstance(MobEffects.GLOWING, durationTicks, 0, false, false, false));
        }
    }

    private static void executePredator(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        UUID playerId = player.getUUID();
        ServerLevel level = (ServerLevel) player.level();
        UUID currentMark = MARKED_TARGETS.get(playerId);
        if (currentMark != null) {
            LivingEntity le;
            LivingEntity marked;
            Entity rawEntity = level.getEntity(currentMark);
            LivingEntity livingEntity = marked = rawEntity instanceof LivingEntity ? (le = (LivingEntity)rawEntity) : null;
            if (marked != null && marked.isAlive() && marked.distanceTo((Entity)player) < 50.0f) {
                if (player.tickCount % 20 == 0) {
                    marked.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false, false));
                }
                return;
            }
            MARKED_TARGETS.remove(playerId);
        }
        if (player.tickCount % 20 != 0) {
            return;
        }
        AABB area = new AABB(player.blockPosition()).inflate(20.0);
        List<Monster> hostiles = level.getEntitiesOfClass(Monster.class, area, e -> e.isAlive());
        Monster nearest = hostiles.stream().min(Comparator.comparingDouble(e -> e.distanceTo((Entity)player))).orElse(null);
        if (nearest != null) {
            MARKED_TARGETS.put(playerId, nearest.getUUID());
            nearest.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false, false));
        }
    }
}

