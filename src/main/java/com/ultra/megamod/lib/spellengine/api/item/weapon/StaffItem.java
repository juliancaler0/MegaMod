package com.ultra.megamod.lib.spellengine.api.item.weapon;

import com.ultra.megamod.feature.relics.data.WeaponRarity;
import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class StaffItem extends Item {

    public StaffItem(Item.Properties settings) {
        super(settings);
    }

    public StaffItem(com.ultra.megamod.lib.spellengine.rpg_series.item.Weapon.CustomMaterial material, Item.Properties settings) {
        super(settings);
    }

    // hurtEnemy is called when the item is used to hit an entity
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
    }

    /**
     * On first server-side inventory tick, roll rarity and bonus attributes via
     * {@link WeaponStatRoller}, layering them on top of the source-derived base
     * modifiers (attack_damage, attack_speed, school spell-power bonuses) baked
     * into the staff/wand's {@code ATTRIBUTE_MODIFIERS} component by
     * {@code Weapon.attributesFrom(WeaponConfig)}. Mirrors the armor pattern.
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

    // ============================================================
    // Right-click cast wiring (parity with source SpellEngine)
    // ============================================================
    // Source SpellEngine's StaffItem has NO use()/getUseAnimation overrides.
    // Casts are dispatched by the SpellHotbar mixin, which reacts to useKey
    // state. When the weapon has NO use animation, itemUseExpectation is null,
    // onUseKey stays null, and the hotbar overrides the spell's keybinding to
    // the useKey — so right-click directly triggers the cast. Adding a BOW
    // animation here breaks that override because itemUseExpectation becomes
    // non-null, and spells route to ALT+N instead.
}
