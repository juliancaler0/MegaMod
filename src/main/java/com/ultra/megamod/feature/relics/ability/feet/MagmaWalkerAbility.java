/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 */
package com.ultra.megamod.feature.relics.ability.feet;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class MagmaWalkerAbility {
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Heat Shield", "Grants fire resistance", 1, RelicAbility.CastType.PASSIVE, List.of()), new RelicAbility("Lava Walk", "Converts lava to obsidian at your feet", 3, RelicAbility.CastType.PASSIVE, List.of()), new RelicAbility("Eruption", "Fire AOE that ignites nearby mobs", 6, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("damage", 5.0, 12.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.12), new RelicStat("radius", 3.0, 6.0, RelicStat.ScaleType.ADD, 0.5))));

    public static void register() {
        AbilityCastHandler.registerAbility("Magma Walker", "Heat Shield", MagmaWalkerAbility::executeHeatShield);
        AbilityCastHandler.registerAbility("Magma Walker", "Lava Walk", MagmaWalkerAbility::executeLavaWalk);
        AbilityCastHandler.registerAbility("Magma Walker", "Eruption", MagmaWalkerAbility::executeEruption);
    }

    private static void executeHeatShield(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 100 != 0) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 120, 0, false, false, true));
    }

    private static void executeLavaWalk(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 5 != 0) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        BlockPos playerPos = player.blockPosition();
        int radius = 3;
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dz = -radius; dz <= radius; ++dz) {
                BlockPos checkPos;
                BlockState state;
                if (dx * dx + dz * dz > radius * radius || !(state = level.getBlockState(checkPos = playerPos.offset(dx, -1, dz))).is(Blocks.LAVA)) continue;
                level.setBlockAndUpdate(checkPos, Blocks.OBSIDIAN.defaultBlockState());
            }
        }
    }

    private static void executeEruption(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double damage = stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity != player && entity.isAlive() && (double)entity.distanceTo((Entity)player) <= radius);
        for (LivingEntity entity2 : entities) {
            entity2.hurt(level.damageSources().playerAttack((Player)player), (float)damage);
            entity2.igniteForSeconds(5.0f);
        }
    }
}

