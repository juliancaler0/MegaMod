package com.ultra.megamod.lib.spellengine.api.item.weapon;

import com.ultra.megamod.feature.relics.data.WeaponRarity;
import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class SpellWeaponItem extends Item {
    public SpellWeaponItem(Item.Properties settings) {
        super(settings);
    }

    public SpellWeaponItem(com.ultra.megamod.lib.spellengine.rpg_series.item.Weapon.CustomMaterial material, Item.Properties settings) {
        super(settings);
    }

    /**
     * On first server-side inventory tick, roll rarity and bonus attributes via
     * {@link WeaponStatRoller}, layering them on top of the source-derived base
     * modifiers baked into the item's {@code ATTRIBUTE_MODIFIERS} component
     * (attack_damage, attack_speed, school spell-power bonuses set by
     * {@code Weapon.attributesFrom(WeaponConfig)}). Mirrors the armor pattern.
     */
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot equipSlot) {
        if (!WeaponStatRoller.isWeaponInitialized(stack)) {
            ItemAttributeModifiers base = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            WeaponStatRoller.rollAndApplyPreservingBase(stack, base, level.random, false);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        WeaponStatRoller.appendWeaponTooltip(stack, tooltip);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        if (WeaponStatRoller.isWeaponInitialized(stack)) {
            WeaponRarity rarity = WeaponStatRoller.getRarity(stack);
            return rarity == WeaponRarity.MYTHIC || rarity == WeaponRarity.LEGENDARY;
        }
        return super.isFoil(stack);
    }
}
