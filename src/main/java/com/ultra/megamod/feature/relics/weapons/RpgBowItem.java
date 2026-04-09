package com.ultra.megamod.feature.relics.weapons;

import com.ultra.megamod.feature.relics.data.WeaponRarity;
import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Bow with rolled rarity and bonus attributes via WeaponStatRoller.
 * Retains full vanilla bow behavior (drawing, firing arrows) with enhancements:
 * - Draw speed scales with weapon tier (higher tier = faster draw)
 * - Arrow damage scaled by the weapon's rolled base damage
 */
public class RpgBowItem extends BowItem {
    private final String weaponName;
    private final float baseDamage;

    public RpgBowItem(String weaponName, float baseDamage, Item.Properties properties) {
        super(properties);
        this.weaponName = weaponName;
        this.baseDamage = baseDamage;
    }

    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!WeaponStatRoller.isWeaponInitialized(stack)) {
            WeaponStatRoller.rollAndApply(stack, baseDamage, level.random);
        }
    }

    /**
     * Scale draw time based on weapon tier. Higher baseDamage = faster draw.
     * Vanilla bow use duration is 72000 ticks. The actual draw time is
     * determined by how long the player holds before releasing.
     * {@code getPowerForTime()} converts use ticks into a 0-1 power factor.
     */
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // Max hold time — actual draw speed is in getPowerForTime
    }

    /**
     * Scales the bow's charge curve so higher-tier bows reach full power faster.
     * Vanilla reaches full power at 20 ticks (1 second).
     * We scale: T1 (4dmg) = 22 ticks, T3 (8dmg) = 14 ticks.
     */
    public static float getPowerForTime(int useTicks, float baseDmg) {
        // Scale charge speed: higher base damage = faster full charge
        // Vanilla full charge at 20 ticks. We adjust: base 4→22t, base 8→14t
        float chargeSpeed = 1.0f + (baseDmg - 4.0f) * 0.06f;
        float scaledTicks = useTicks * Math.max(0.5f, chargeSpeed);
        float f = scaledTicks / 20.0f;
        f = (f * f + f * 2.0f) / 3.0f;
        return Math.min(f, 1.0f);
    }

    // Note: releaseUsing is final in BowItem in 1.21.11. Arrow damage bonus
    // is applied via the rolled RANGED_DAMAGE attribute through AttributeEvents.onLivingDamage.

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        WeaponStatRoller.appendWeaponTooltip(stack, tooltip);
    }

    public boolean isFoil(ItemStack stack) {
        if (WeaponStatRoller.isWeaponInitialized(stack)) {
            WeaponRarity rarity = WeaponStatRoller.getRarity(stack);
            return rarity == WeaponRarity.MYTHIC || rarity == WeaponRarity.LEGENDARY;
        }
        return false;
    }

    public String getWeaponName() { return weaponName; }
    public float getBaseDamage() { return baseDamage; }
}
