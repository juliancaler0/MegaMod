package com.ultra.megamod.feature.citizen.raid;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumSet;

/**
 * Ranged combat goal for archer raiders.
 * Fires arrows at the current target with an adjustable fire rate.
 */
public class RaiderRangedAI extends Goal {

    private final Monster mob;
    private final double moveSpeed;
    private final int attackIntervalTicks;
    private final float attackRadiusSq;
    private int attackCooldown;
    private int seeTargetTicks;

    /**
     * @param mob                 the raider entity
     * @param moveSpeed           speed to approach target
     * @param attackIntervalTicks ticks between shots
     * @param attackRadius        max distance to fire
     */
    public RaiderRangedAI(Monster mob, double moveSpeed, int attackIntervalTicks, float attackRadius) {
        this.mob = mob;
        this.moveSpeed = moveSpeed;
        this.attackIntervalTicks = attackIntervalTicks;
        this.attackRadiusSq = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.attackCooldown = 0;
        this.seeTargetTicks = 0;
    }

    @Override
    public void stop() {
        this.seeTargetTicks = 0;
        this.attackCooldown = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        double distSq = mob.distanceToSqr(target);
        boolean canSee = mob.getSensing().hasLineOfSight(target);

        if (canSee) {
            seeTargetTicks++;
        } else {
            seeTargetTicks = 0;
        }

        // Move toward target if too far or can't see them
        if (distSq > attackRadiusSq || seeTargetTicks < 5) {
            mob.getNavigation().moveTo(target, moveSpeed);
        } else {
            mob.getNavigation().stop();
        }

        mob.getLookControl().setLookAt(target, 30.0f, 30.0f);

        if (--attackCooldown <= 0) {
            if (distSq <= attackRadiusSq && canSee) {
                attackCooldown = attackIntervalTicks;
                performRangedAttack(target);
            }
        }
    }

    private void performRangedAttack(LivingEntity target) {
        if (mob.level().isClientSide()) return;
        ServerLevel serverLevel = (ServerLevel) mob.level();

        Arrow arrow = new Arrow(mob.level(), mob, new ItemStack(Items.ARROW), null);

        double dx = target.getX() - mob.getX();
        double dy = target.getY(0.3333) - arrow.getY();
        double dz = target.getZ() - mob.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        arrow.shoot(dx, dy + dist * 0.2, dz, 1.6f, (float) (14 - serverLevel.getDifficulty().getId() * 4));
        // Arrow damage is determined by bow power enchantment or base arrow damage

        mob.playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (mob.getRandom().nextFloat() * 0.4f + 0.8f));
        serverLevel.addFreshEntity(arrow);
    }
}
