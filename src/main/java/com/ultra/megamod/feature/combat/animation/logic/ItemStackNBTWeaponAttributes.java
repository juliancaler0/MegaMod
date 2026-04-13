package com.ultra.megamod.feature.combat.animation.logic;

import com.ultra.megamod.feature.combat.animation.WeaponAttributes;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for attaching weapon attributes to ItemStacks via mixin.
 * Ported 1:1 from BetterCombat (net.bettercombat.logic.ItemStackNBTWeaponAttributes).
 */
public interface ItemStackNBTWeaponAttributes {
    boolean hasInvalidAttributes();
    void setInvalidAttributes(boolean invalid);

    @Nullable
    WeaponAttributes getWeaponAttributes();
    void setWeaponAttributes(@Nullable WeaponAttributes weaponAttributes);
}
