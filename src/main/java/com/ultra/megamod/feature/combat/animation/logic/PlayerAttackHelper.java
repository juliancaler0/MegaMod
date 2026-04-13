package com.ultra.megamod.feature.combat.animation.logic;

import com.ultra.megamod.feature.combat.animation.AttackHand;
import com.ultra.megamod.feature.combat.animation.ComboState;
import com.ultra.megamod.feature.combat.animation.WeaponAttributeRegistry;
import com.ultra.megamod.feature.combat.animation.WeaponAttributes;
import com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Attack helper with dual-wielding, combo selection, range, and pose logic.
 * Ported 1:1 from BetterCombat (net.bettercombat.logic.PlayerAttackHelper).
 */
public class PlayerAttackHelper {

    public static float getDualWieldingAttackDamageMultiplier(Player player, AttackHand hand) {
        return isDualWielding(player)
                ? (hand.isOffHand()
                    ? BetterCombatConfig.dual_wielding_off_hand_damage_multiplier
                    : BetterCombatConfig.dual_wielding_main_hand_damage_multiplier)
                : 1;
    }

    public static boolean shouldAttackWithOffHand(Player player, int comboCount) {
        return isDualWielding(player) && comboCount % 2 == 1;
    }

    public static boolean isDualWielding(Player player) {
        var mainAttrs = WeaponAttributeRegistry.getAttributes(player.getMainHandItem());
        var offAttrs = WeaponAttributeRegistry.getAttributes(player.getOffhandItem());
        return isDualWielding(mainAttrs, offAttrs);
    }

    public static boolean isDualWielding(WeaponAttributes mainAttrs, WeaponAttributes offAttrs) {
        return mainAttrs != null && !mainAttrs.twoHanded()
                && offAttrs != null && !offAttrs.twoHanded();
    }

    public static boolean isTwoHandedWielding(Player player) {
        var mainAttrs = WeaponAttributeRegistry.getAttributes(player.getMainHandItem());
        return mainAttrs != null && mainAttrs.twoHanded();
    }

    public static float getAttackCooldownTicksCapped(Player player) {
        float base = Math.max(player.getCurrentItemAttackStrengthDelay(), BetterCombatConfig.attack_interval_cap);
        // Apply COMBO_SPEED as a haste multiplier so player buffs/jewelry speed up swings.
        // COMBO_SPEED is expressed as percent (e.g. 25.0 = +25% faster).
        double comboSpeed = player.getAttributeValue(
                com.ultra.megamod.feature.attributes.MegaModAttributes.COMBO_SPEED);
        if (comboSpeed > 0) {
            float hasteFactor = 1.0f + (float) (comboSpeed / 100.0);
            base = base / hasteFactor;
        }
        // Clamp to the interval cap again so haste can't push below the minimum
        return Math.max(base, BetterCombatConfig.attack_interval_cap);
    }

    @Nullable
    public static AttackHand getCurrentAttack(Player player, int comboCount) {
        if (isDualWielding(player)) {
            boolean isOffHand = shouldAttackWithOffHand(player, comboCount);
            var itemStack = isOffHand ? player.getOffhandItem() : player.getMainHandItem();
            var attributes = WeaponAttributeRegistry.getAttributes(itemStack);
            if (attributes != null && attributes.attacks() != null) {
                int handSpecificCombo = ((isOffHand && comboCount > 0) ? (comboCount - 1) : comboCount) / 2;
                var selection = selectAttack(handSpecificCombo, attributes, player, isOffHand);
                if (selection == null) return null;
                return new AttackHand(selection.attack, selection.comboState, isOffHand, attributes, itemStack);
            }
        } else {
            var itemStack = player.getMainHandItem();
            var attributes = WeaponAttributeRegistry.getAttributes(itemStack);
            if (attributes != null && attributes.attacks() != null) {
                var selection = selectAttack(comboCount, attributes, player, false);
                if (selection == null) return null;
                return new AttackHand(selection.attack, selection.comboState, false, attributes, itemStack);
            }
        }
        return null;
    }

    private record AttackSelection(WeaponAttributes.Attack attack, ComboState comboState) {}

    @Nullable
    private static AttackSelection selectAttack(int comboCount, WeaponAttributes attributes, Player player, boolean isOffHand) {
        var attacks = attributes.attacks();
        // Filter by conditions
        attacks = Arrays.stream(attacks)
                .filter(attack -> attack.conditions() == null || attack.conditions().length == 0
                        || evaluateConditions(attack.conditions(), player, isOffHand))
                .toArray(WeaponAttributes.Attack[]::new);
        if (comboCount < 0) comboCount = 0;
        if (attacks.length == 0) return null;
        int index = comboCount % attacks.length;
        return new AttackSelection(attacks[index], new ComboState(index + 1, attacks.length));
    }

    private static boolean evaluateConditions(WeaponAttributes.Condition[] conditions, Player player, boolean isOffHand) {
        return Arrays.stream(conditions).allMatch(c -> evaluateCondition(c, player, isOffHand));
    }

    private static boolean evaluateCondition(WeaponAttributes.Condition condition, Player player, boolean isOffHand) {
        if (condition == null) return true;
        return switch (condition) {
            case NOT_DUAL_WIELDING -> !isDualWielding(player);
            case DUAL_WIELDING_ANY -> isDualWielding(player);
            case DUAL_WIELDING_SAME_CATEGORY -> {
                if (!isDualWielding(player)) yield false;
                var mainAttrs = WeaponAttributeRegistry.getAttributes(player.getMainHandItem());
                var offAttrs = WeaponAttributeRegistry.getAttributes(player.getOffhandItem());
                yield mainAttrs != null && offAttrs != null
                        && mainAttrs.category() != null && !mainAttrs.category().isEmpty()
                        && mainAttrs.category().equals(offAttrs.category());
            }
            case NO_OFFHAND_ITEM -> player.getOffhandItem().isEmpty();
            case OFF_HAND_SHIELD -> player.getOffhandItem().getItem() instanceof net.minecraft.world.item.ShieldItem;
            case MAIN_HAND_ONLY -> !isOffHand;
            case OFF_HAND_ONLY -> isOffHand;
            case MOUNTED -> player.getVehicle() != null;
            case NOT_MOUNTED -> player.getVehicle() == null;
        };
    }

    public static Pose poseForPlayer(Player player) {
        var mainAttrs = WeaponAttributeRegistry.getAttributes(player.getMainHandItem());
        String mainPose = (mainAttrs != null && mainAttrs.pose() != null) ? mainAttrs.pose() : "";

        var offAttrs = WeaponAttributeRegistry.getAttributes(player.getOffhandItem());
        String offPose = "";
        if (isDualWielding(mainAttrs, offAttrs) && offAttrs != null && offAttrs.pose() != null) {
            offPose = offAttrs.pose();
        }
        return new Pose(mainPose, offPose);
    }

    public static double getAttackRange(Player player) {
        var mainAttrs = WeaponAttributeRegistry.getAttributes(player.getMainHandItem());
        double baseRange = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        if (mainAttrs != null) {
            if (mainAttrs.attackRange() != 0) return mainAttrs.attackRange();
            baseRange += mainAttrs.rangeBonus();
        }
        return baseRange;
    }

    /**
     * Returns the interaction range modified by the weapon's range bonus.
     * Used by PlayerEntityRangeMixin to augment getEntityInteractionRange.
     * Ported from BetterCombat (net.bettercombat.logic.PlayerAttackHelper.getRangeWithWeapon).
     */
    public static double getRangeWithWeapon(Player player, double interactionRangeValue) {
        return getRangeWithItem(player.getMainHandItem(), interactionRangeValue);
    }

    private static double getRangeWithItem(net.minecraft.world.item.ItemStack stack, double interactionRangeValue) {
        if (EntityAttributeHelper.itemHasRangeAttribute(stack)) {
            return interactionRangeValue;
        }
        var attributes = WeaponAttributeRegistry.getAttributes(stack);
        if (attributes != null) {
            if (attributes.attackRange() != 0) {
                return attributes.attackRange();
            }
            return interactionRangeValue + attributes.rangeBonus();
        }
        return interactionRangeValue;
    }

    /**
     * Swaps main hand and off-hand attributes for off-hand attacks.
     * The runnable executes with off-hand stats applied, then restores original state.
     * Ported 1:1 from BetterCombat's PlayerAttackHelper.swapHandAttributes.
     */
    public static void swapHandAttributes(Player player, boolean useOffHand, Runnable runnable) {
        if (!useOffHand) {
            runnable.run();
            return;
        }
        synchronized (player) {
            var inventory = player.getInventory();
            var mainHandStack = player.getMainHandItem();
            var offHandStack = InventoryUtil.getOffHandSlotStack(player);

            // Remove main-hand modifiers, add off-hand modifiers
            setAttributesForOffHandAttack(player, true);
            // Swap the actual items
            inventory.setItem(inventory.getSelectedSlot(), offHandStack);
            InventoryUtil.setOffHandSlotStack(player, mainHandStack);

            runnable.run();

            // Restore items
            inventory.setItem(inventory.getSelectedSlot(), mainHandStack);
            InventoryUtil.setOffHandSlotStack(player, offHandStack);
            // Restore modifiers
            setAttributesForOffHandAttack(player, false);
        }
    }

    private static void setAttributesForOffHandAttack(Player player, boolean useOffHand) {
        var mainHandStack = player.getMainHandItem();
        var offHandStack = player.getOffhandItem();
        net.minecraft.world.item.ItemStack add;
        net.minecraft.world.item.ItemStack remove;
        if (useOffHand) {
            remove = mainHandStack;
            add = offHandStack;
        } else {
            remove = offHandStack;
            add = mainHandStack;
        }
        if (remove != null && !remove.isEmpty()) {
            var modifiers = com.ultra.megamod.feature.combat.animation.utils.AttributeModifierHelper.modifierMultimap(remove);
            player.getAttributes().removeAttributeModifiers(modifiers);
        }
        if (add != null && !add.isEmpty()) {
            var modifiers = com.ultra.megamod.feature.combat.animation.utils.AttributeModifierHelper.modifierMultimap(add);
            player.getAttributes().addTransientAttributeModifiers(modifiers);
        }
    }
}
