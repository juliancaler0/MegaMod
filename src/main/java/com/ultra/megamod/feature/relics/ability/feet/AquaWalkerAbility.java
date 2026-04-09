/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.ultra.megamod.feature.relics.ability.feet;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AquaWalkerAbility {
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Water Walk", "Walk on water surfaces", 1, RelicAbility.CastType.PASSIVE, List.of()), new RelicAbility("Ripple", "Push nearby entities away with water force", 4, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("push_force", 1.0, 3.0, RelicStat.ScaleType.ADD, 0.3), new RelicStat("radius", 4.0, 8.0, RelicStat.ScaleType.ADD, 0.5))));

    public static void register() {
        AbilityCastHandler.registerAbility("Aqua Walker", "Water Walk", AquaWalkerAbility::executeWaterWalk);
        AbilityCastHandler.registerAbility("Aqua Walker", "Ripple", AquaWalkerAbility::executeRipple);
    }

    private static void executeWaterWalk(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        boolean onWaterSurface;
        if (player.isShiftKeyDown()) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        BlockPos belowPos = player.blockPosition().below();
        BlockState belowState = level.getBlockState(belowPos);
        BlockState atFeetState = level.getBlockState(player.blockPosition());
        boolean bl = onWaterSurface = belowState.is(Blocks.WATER) && (atFeetState.isAir() || atFeetState.is(Blocks.WATER));
        if (onWaterSurface) {
            Vec3 motion = player.getDeltaMovement();
            if (motion.y < 0.0) {
                player.setDeltaMovement(motion.x, 0.0, motion.z);
                player.hurtMarked = true;
            }
            double surfaceY = (double)belowPos.getY() + 1.0;
            if (player.getY() < surfaceY) {
                player.teleportTo(player.getX(), surfaceY, player.getZ());
            }
            player.fallDistance = 0.0;
            player.setOnGround(true);
        }
    }

    private static void executeRipple(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double pushForce = stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        boolean nearWater = false;
        BlockPos playerPos = player.blockPosition();
        for (int dx = -3; dx <= 3 && !nearWater; ++dx) {
            for (int dy = -2; dy <= 2 && !nearWater; ++dy) {
                for (int dz = -3; dz <= 3 && !nearWater; ++dz) {
                    if (!level.getBlockState(playerPos.offset(dx, dy, dz)).is(Blocks.WATER)) continue;
                    nearWater = true;
                }
            }
        }
        if (!nearWater) {
            return;
        }
        AABB area = new AABB(playerPos).inflate(radius);
        List<Entity> entities = level.getEntities(player, area, entity -> entity.isAlive() && (double)entity.distanceTo((Entity)player) <= radius);
        Vec3 playerVec = player.position();
        for (Entity entity2 : entities) {
            Vec3 direction = entity2.position().subtract(playerVec).normalize();
            entity2.setDeltaMovement(entity2.getDeltaMovement().add(direction.x * pushForce, 0.3 * pushForce, direction.z * pushForce));
            entity2.hurtMarked = true;
        }
    }
}

