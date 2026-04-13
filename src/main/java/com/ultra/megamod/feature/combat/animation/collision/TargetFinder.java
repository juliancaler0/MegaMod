package com.ultra.megamod.feature.combat.animation.collision;

import com.ultra.megamod.feature.combat.animation.WeaponAttributes;
import com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig;
import com.ultra.megamod.feature.combat.animation.logic.TargetHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Finds entities within weapon hitbox using OBB intersection + radial filter.
 * Ported 1:1 from BetterCombat (net.bettercombat.client.collision.TargetFinder).
 */
public class TargetFinder {

    public static class TargetResult {
        public Entity cursorTarget;
        public List<Entity> entities;
        public OrientedBoundingBox obb;

        public TargetResult(@Nullable Entity cursorTarget, List<Entity> entities, OrientedBoundingBox obb) {
            this.cursorTarget = cursorTarget;
            this.entities = entities;
            this.obb = obb;
        }
    }

    public static TargetResult findAttackTargetResult(Player player, @Nullable Entity cursorTarget,
                                                       WeaponAttributes.Attack attack, double attackRange) {
        Vec3 origin = getInitialTracingPoint(player);
        List<Entity> entities = getInitialTargets(player, cursorTarget, attackRange);

        boolean isSpinAttack = attack.angle() > 180;
        Vec3 size = WeaponHitBoxes.createHitbox(attack.hitbox(), attackRange, isSpinAttack);
        var obb = new OrientedBoundingBox(origin, size, player.getXRot(), player.getYRot());
        if (!isSpinAttack) {
            obb = obb.offsetAlongAxisZ(size.z / 2F);
        }
        obb.updateVertex();

        // Filter by OBB intersection
        final OrientedBoundingBox finalObb = obb;
        entities = entities.stream()
                .filter(entity -> finalObb.intersects(entity.getBoundingBox().inflate(entity.getPickRadius()))
                        || finalObb.contains(entity.position().add(0, entity.getBbHeight() / 2F, 0)))
                .collect(Collectors.toList());

        // Filter by angle
        final Vec3 finalOrigin = origin;
        final Vec3 orientation = obb.axisZ;
        entities = entities.stream()
                .filter(entity -> {
                    double maxAngleDif = attack.angle() / 2.0;
                    Vec3 distVec = CollisionHelper.distanceVector(finalOrigin, entity.getBoundingBox());
                    Vec3 posVec = entity.position().add(0, entity.getBbHeight() / 2F, 0).subtract(finalOrigin);
                    return distVec.length() <= attackRange
                            && (attack.angle() == 0
                                || CollisionHelper.angleBetween(posVec, orientation) <= maxAngleDif
                                || CollisionHelper.angleBetween(distVec, orientation) <= maxAngleDif)
                            && (BetterCombatConfig.allow_attacking_thru_walls
                                || rayContainsNoObstacle(finalOrigin, finalOrigin.add(distVec))
                                || rayContainsNoObstacle(finalOrigin, finalOrigin.add(posVec)));
                })
                .collect(Collectors.toList());

        return new TargetResult(cursorTarget, entities, obb);
    }

    public static List<Entity> findAttackTargets(Player player, @Nullable Entity cursorTarget,
                                                  WeaponAttributes.Attack attack, double attackRange) {
        return findAttackTargetResult(player, cursorTarget, attack, attackRange).entities;
    }

    public static Vec3 getInitialTracingPoint(Player player) {
        double shoulderHeight = player.getBbHeight() * 0.15 * player.getScale();
        return player.getEyePosition().subtract(0, shoulderHeight, 0);
    }

    public static List<Entity> getInitialTargets(Player player, @Nullable Entity cursorTarget, double attackRange) {
        AABB box = player.getBoundingBox().inflate(
                attackRange * com.ultra.megamod.feature.combat.animation.config.ScopedCombatConfig.targetSearchRangeMultiplier(player) + 1.0);
        List<Entity> entities = player.level()
                .getEntities(player, box, entity -> !entity.isSpectator() && entity.isPickable())
                .stream()
                .filter(entity -> entity != player
                        && entity.isAttackable()
                        && entity != cursorTarget
                        && TargetHelper.isHitAllowed(false, TargetHelper.getRelation(player, entity))
                        && (!entity.equals(player.getVehicle()) || TargetHelper.isAttackableMount(entity)))
                .collect(Collectors.toList());
        if (cursorTarget != null && cursorTarget.isAttackable()) {
            entities.add(cursorTarget);
        }
        return entities;
    }

    private static boolean rayContainsNoObstacle(Vec3 start, Vec3 end) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return true;
        BlockHitResult hit = mc.level.clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        return hit == null || hit.getType() != HitResult.Type.BLOCK;
    }
}
