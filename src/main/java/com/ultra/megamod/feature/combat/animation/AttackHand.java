package com.ultra.megamod.feature.combat.animation;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Snapshot of the current attack state for a player's hand.
 * Combines the resolved attack from the weapon's combo sequence with
 * context about which hand, which combo position, and which item is being used.
 *
 * @param attack     The specific attack being performed from the weapon's combo sequence.
 * @param combo      The current combo state (position within the attack sequence).
 * @param isOffHand  True if this attack is being performed with the off-hand.
 * @param attributes The full weapon attributes for the item being swung.
 * @param itemStack  The actual ItemStack being used for this attack.
 */
public record AttackHand(
        WeaponAttributes.Attack attack,
        ComboState combo,
        boolean isOffHand,
        WeaponAttributes attributes,
        ItemStack itemStack
) {

    /**
     * Returns the effective upswing rate for this attack, clamped to 0.0-1.0.
     * The upswing determines when during the swing animation the damage is applied.
     */
    public double upswingRate() {
        return Mth.clamp(attack.upswing(), 0.0, 1.0);
    }

    /**
     * Returns the effective damage multiplier for this individual attack.
     */
    public double damageMultiplier() {
        return attack.damageMultiplier();
    }

    /**
     * Returns the effective attack range (base entity range + weapon bonus).
     */
    public double effectiveRange(double baseRange) {
        return baseRange + attributes.rangeBonus();
    }
}
