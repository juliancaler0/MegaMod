package com.ultra.megamod.mixin.bettercombat;

import com.ultra.megamod.feature.combat.animation.WeaponAttributes;
import com.ultra.megamod.feature.combat.animation.logic.ItemStackNBTWeaponAttributes;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Attaches weapon attributes to ItemStack instances for per-stack attribute caching.
 * Ported 1:1 from BetterCombat (net.bettercombat.mixin.ItemStackMixin).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackNBTWeaponAttributes {

    @Unique
    private boolean bettercombat$hasInvalidAttributes = false;

    @Override
    public boolean hasInvalidAttributes() {
        return bettercombat$hasInvalidAttributes;
    }

    @Override
    public void setInvalidAttributes(boolean invalid) {
        this.bettercombat$hasInvalidAttributes = invalid;
    }

    @Unique
    private WeaponAttributes bettercombat$weaponAttributes;

    @Override
    public WeaponAttributes getWeaponAttributes() {
        return bettercombat$weaponAttributes;
    }

    @Override
    public void setWeaponAttributes(WeaponAttributes weaponAttributes) {
        this.bettercombat$weaponAttributes = weaponAttributes;
    }
}
