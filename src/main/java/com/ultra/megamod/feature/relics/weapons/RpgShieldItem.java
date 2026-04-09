package com.ultra.megamod.feature.relics.weapons;

import com.ultra.megamod.feature.relics.data.WeaponRarity;
import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Shield with rolled rarity and bonus attributes via WeaponStatRoller.
 * Retains full vanilla shield blocking behavior.
 * Uses isShield=true so bonus stats apply from any slot (offhand).
 */
public class RpgShieldItem extends ShieldItem {
    private final String shieldName;
    private final float baseDamage;

    public RpgShieldItem(String shieldName, float baseDamage, Item.Properties properties) {
        super(properties);
        this.shieldName = shieldName;
        this.baseDamage = baseDamage;
    }

    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!WeaponStatRoller.isWeaponInitialized(stack)) {
            WeaponStatRoller.rollAndApply(stack, baseDamage, level.random, true);
        }
    }

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

    public String getShieldName() { return shieldName; }
    public float getBaseDamage() { return baseDamage; }
}
