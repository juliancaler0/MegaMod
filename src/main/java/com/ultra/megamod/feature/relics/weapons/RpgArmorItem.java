package com.ultra.megamod.feature.relics.weapons;

import com.ultra.megamod.feature.relics.data.ArmorStatRoller;
import com.ultra.megamod.feature.relics.data.WeaponRarity;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Armor item with rolled rarity and bonus attributes via ArmorStatRoller.
 * On first inventory tick, rolls rarity + bonus stats and applies them.
 */
public class RpgArmorItem extends Item {
    private final double baseArmor;
    private final double baseToughness;
    private final EquipmentSlot armorSlot;

    public RpgArmorItem(double baseArmor, double baseToughness, EquipmentSlot armorSlot, Item.Properties properties) {
        super(properties);
        this.baseArmor = baseArmor;
        this.baseToughness = baseToughness;
        this.armorSlot = armorSlot;
    }

    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!ArmorStatRoller.isArmorInitialized(stack)) {
            ArmorStatRoller.rollAndApply(stack, baseArmor, baseToughness, armorSlot, level.random);
        }
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        ArmorStatRoller.appendArmorTooltip(stack, tooltip);
    }

    public boolean isFoil(ItemStack stack) {
        if (ArmorStatRoller.isArmorInitialized(stack)) {
            WeaponRarity rarity = ArmorStatRoller.getRarity(stack);
            return rarity == WeaponRarity.MYTHIC || rarity == WeaponRarity.LEGENDARY;
        }
        return false;
    }

    public double getBaseArmor() { return baseArmor; }
    public double getBaseToughness() { return baseToughness; }
    public EquipmentSlot getArmorSlot() { return armorSlot; }
}
