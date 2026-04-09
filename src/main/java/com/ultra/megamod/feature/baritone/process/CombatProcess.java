package com.ultra.megamod.feature.baritone.process;

import com.ultra.megamod.feature.baritone.goals.Goal;
import com.ultra.megamod.feature.baritone.goals.GoalBlock;
import com.ultra.megamod.feature.baritone.goals.GoalInverted;
import com.ultra.megamod.feature.baritone.goals.GoalNear;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

/**
 * Process: engage hostile mobs in combat.
 * Scans for hostiles, navigates toward them, attacks with best weapon,
 * eats food when low health, retreats when critically low.
 */
public class CombatProcess implements BotProcess {
    private boolean active = false;
    private String status = "Idle";
    private ServerPlayer player;
    private ServerLevel level;
    private int scanRadius = 16;
    private int kills = 0;

    // Combat state
    private LivingEntity currentTarget;
    private int attackCooldown = 0;
    private boolean retreating = false;

    // Thresholds
    private static final float EAT_HEALTH_THRESHOLD = 10.0f; // Eat when below 5 hearts
    private static final float RETREAT_HEALTH_THRESHOLD = 4.0f; // Retreat when below 2 hearts
    private static final double ATTACK_RANGE_SQ = 4.0 * 4.0; // 4 block attack range squared
    private static final int ATTACK_COOLDOWN_TICKS = 10; // Swing every 10 ticks

    public void start(ServerPlayer player, ServerLevel level, int radius) {
        this.player = player;
        this.level = level;
        this.scanRadius = radius;
        this.kills = 0;
        this.currentTarget = null;
        this.attackCooldown = 0;
        this.retreating = false;
        this.active = true;
        this.status = "Combat mode (radius " + radius + ")";
    }

    @Override
    public String name() { return "Combat"; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public double priority() { return 80; } // High priority — combat is urgent

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean safeToCancel) {
        if (!active || player == null || level == null) return null;

        float health = player.getHealth();

        // Auto-eat when health is low
        if (health < EAT_HEALTH_THRESHOLD && !retreating) {
            tryEatFood();
        }

        // Retreat mode: health critically low
        if (health < RETREAT_HEALTH_THRESHOLD) {
            retreating = true;
            currentTarget = null;
            status = "Retreating! (HP=" + String.format("%.1f", health) + ", " + kills + " kills)";

            // Find nearest hostile to flee FROM
            LivingEntity nearest = findNearestHostile();
            if (nearest != null) {
                Goal fleeFrom = new GoalBlock(
                    nearest.blockPosition().getX(),
                    nearest.blockPosition().getY(),
                    nearest.blockPosition().getZ()
                );
                Goal retreatGoal = new GoalInverted(fleeFrom, 20);
                return new PathingCommand(retreatGoal, PathingCommand.CommandType.SET_GOAL_AND_PATH);
            }
            // No hostiles nearby while retreating — stop retreating
            retreating = false;
        }

        // If health recovered, stop retreating
        if (retreating && health >= EAT_HEALTH_THRESHOLD) {
            retreating = false;
        }

        if (retreating) {
            // Still retreating, try eating again
            tryEatFood();
            return new PathingCommand(null, PathingCommand.CommandType.REQUEST_PAUSE);
        }

        // Validate current target
        if (currentTarget != null) {
            if (currentTarget.isRemoved() || !currentTarget.isAlive() || currentTarget.isDeadOrDying()) {
                // Target died — count kill
                kills++;
                status = "Kill #" + kills + "! Scanning...";
                currentTarget = null;
            } else {
                double distSq = player.distanceToSqr(currentTarget);
                if (distSq > (scanRadius * 2.0) * (scanRadius * 2.0)) {
                    // Target too far, drop it
                    currentTarget = null;
                }
            }
        }

        // Find new target if needed
        if (currentTarget == null) {
            currentTarget = findNearestHostile();
            if (currentTarget == null) {
                status = "No hostiles nearby (" + kills + " kills)";
                // Stay in combat mode but idle
                return new PathingCommand(null, PathingCommand.CommandType.REQUEST_PAUSE);
            }
        }

        // Equip best weapon
        equipBestWeapon();

        // Check attack range
        double distSq = player.distanceToSqr(currentTarget);
        if (distSq <= ATTACK_RANGE_SQ) {
            // In range — attack
            if (attackCooldown <= 0) {
                player.attack(currentTarget);
                player.resetAttackStrengthTicker();
                attackCooldown = ATTACK_COOLDOWN_TICKS;
                status = "Attacking " + currentTarget.getName().getString()
                    + " (HP=" + String.format("%.1f", currentTarget.getHealth()) + ", " + kills + " kills)";
            } else {
                attackCooldown--;
            }
            // Stay near the target
            Goal goal = new GoalNear(
                currentTarget.blockPosition().getX(),
                currentTarget.blockPosition().getY(),
                currentTarget.blockPosition().getZ(),
                2
            );
            return new PathingCommand(goal, PathingCommand.CommandType.SET_GOAL_AND_PATH);
        }

        // Not in range — navigate toward target
        attackCooldown = Math.max(0, attackCooldown - 1);
        status = "Engaging " + currentTarget.getName().getString()
            + " (dist=" + String.format("%.1f", Math.sqrt(distSq)) + ", " + kills + " kills)";
        Goal goal = new GoalNear(
            currentTarget.blockPosition().getX(),
            currentTarget.blockPosition().getY(),
            currentTarget.blockPosition().getZ(),
            2
        );
        return new PathingCommand(goal, PathingCommand.CommandType.SET_GOAL_AND_PATH);
    }

    private LivingEntity findNearestHostile() {
        AABB scanBox = new AABB(
            player.getX() - scanRadius, player.getY() - scanRadius, player.getZ() - scanRadius,
            player.getX() + scanRadius, player.getY() + scanRadius, player.getZ() + scanRadius
        );

        List<Monster> hostiles = level.getEntitiesOfClass(Monster.class, scanBox,
            e -> e.isAlive() && !e.isDeadOrDying());

        return hostiles.stream()
            .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
            .orElse(null);
    }

    private void equipBestWeapon() {
        int bestSlot = -1;
        float bestDamage = 0;

        // Search hotbar (slots 0-8) for best weapon
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            boolean isSword = path.contains("sword");
            boolean isAxe = path.contains("axe") && !path.contains("pickaxe");
            if (isSword || isAxe) {
                // Approximate damage from tier — swords and axes are the best melee options
                float damage = isSword ? 2.0f : 1.5f;
                // Higher tier tools have more max damage, use that as a proxy
                damage += stack.getMaxDamage() / 100.0f;
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            }
        }

        if (bestSlot >= 0) {
            swapToHotbarSlot(bestSlot);
        }
    }

    /**
     * Swap the item in the given hotbar slot with the player's current mainhand slot.
     * Inventory.selected is private in 1.21.11, so we physically swap ItemStacks.
     */
    private void swapToHotbarSlot(int targetSlot) {
        ItemStack mainHand = player.getMainHandItem();
        int currentSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i) == mainHand) {
                currentSlot = i;
                break;
            }
        }
        if (currentSlot < 0 || currentSlot == targetSlot) return;
        ItemStack targetItem = player.getInventory().getItem(targetSlot);
        ItemStack currentItem = player.getInventory().getItem(currentSlot);
        player.getInventory().setItem(currentSlot, targetItem);
        player.getInventory().setItem(targetSlot, currentItem);
    }

    private void tryEatFood() {
        // Find food in hotbar first, then rest of inventory
        int foodSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.has(DataComponents.FOOD)) {
                foodSlot = i;
                break;
            }
        }
        if (foodSlot < 0) {
            // Check rest of inventory
            for (int i = 9; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.has(DataComponents.FOOD)) {
                    foodSlot = i;
                    break;
                }
            }
        }
        if (foodSlot < 0) return; // No food found

        // Consume one food item and restore hunger directly (server authority)
        ItemStack foodStack = player.getInventory().getItem(foodSlot);
        foodStack.shrink(1);
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0f);
    }

    public int getKills() { return kills; }
    public LivingEntity getCurrentTarget() { return currentTarget; }
    public boolean isRetreating() { return retreating; }

    public void updateState(ServerPlayer player, ServerLevel level) {
        this.player = player;
        this.level = level;
    }

    @Override
    public void onLostControl() {
        // Keep tracking but stop attacking
        attackCooldown = 0;
    }

    @Override
    public void cancel() {
        active = false;
        currentTarget = null;
        retreating = false;
        status = "Combat ended (" + kills + " kills)";
    }

    @Override
    public String getStatus() { return status; }
}
