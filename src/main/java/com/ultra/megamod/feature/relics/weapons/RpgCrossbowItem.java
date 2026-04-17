package com.ultra.megamod.feature.relics.weapons;

import com.ultra.megamod.feature.relics.data.WeaponRarity;
import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Crossbow with rolled rarity and bonus attributes via WeaponStatRoller.
 * Retains full vanilla crossbow behavior (charging, firing).
 * Higher-tier crossbows charge faster via the rolled stats system.
 */
public class RpgCrossbowItem extends CrossbowItem {
    private final String weaponName;
    private final float baseDamage;

    public RpgCrossbowItem(String weaponName, float baseDamage, Item.Properties properties) {
        super(properties);
        this.weaponName = weaponName;
        this.baseDamage = baseDamage;
    }

    // Stat rolling disabled — source-parity attributes only (rolls come later).
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        // no-op
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    public boolean isFoil(ItemStack stack) {
        return false;
    }

    public String getWeaponName() { return weaponName; }
    public float getBaseDamage() { return baseDamage; }
}
