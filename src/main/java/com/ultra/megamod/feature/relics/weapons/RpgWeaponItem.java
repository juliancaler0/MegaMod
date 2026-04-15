/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.component.TooltipDisplay
 */
package com.ultra.megamod.feature.relics.weapons;

import com.ultra.megamod.feature.relics.data.WeaponRarity;
import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class RpgWeaponItem
extends Item {
    private final String weaponName;
    private final float baseDamage;
    private final List<WeaponSkill> skills;
    private final boolean isShield;

    public RpgWeaponItem(String weaponName, float baseDamage, Item.Properties properties, List<WeaponSkill> skills) {
        super(properties.stacksTo(1));
        this.weaponName = weaponName;
        this.baseDamage = baseDamage;
        this.skills = skills;
        String lower = weaponName.toLowerCase();
        this.isShield = lower.contains("bulwark") || lower.contains("bastion") || lower.contains("ironwall")
            || lower.contains("aegis") || lower.contains("titan") || lower.contains("ward")
            || lower.contains("stoneguard") || lower.contains("breaker");
    }

    public RpgWeaponItem(String weaponName, Item.Properties properties, List<WeaponSkill> skills) {
        this(weaponName, 5.0f, properties, skills);
    }

    public boolean isShield() {
        return this.isShield;
    }

    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (this.isShield) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        // If this weapon carries a SpellContainer, trigger a cast on right-click.
        // Mirrors SpellEngine's right-click-to-cast UX for magic weapons.
        ItemStack stack = player.getItemInHand(hand);
        var container = stack.get(com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents.SPELL_CONTAINER);
        if (container != null && !container.spell_ids().isEmpty()) {
            // Hotbar mixin owns the actual dispatch; calling startUsingItem lets that
            // mixin see we're in a use interaction and dispatch the cast. Duration
            // uses a long ceiling so it survives charge + channel spells.
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        return super.use(level, player, hand);
    }

    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        if (this.isShield) return ItemUseAnimation.BLOCK;
        // Any spell-carrying weapon needs a non-NONE use animation so vanilla treats
        // the right-click as a held "use" and the SpellHotbar mixin can drive the cast.
        var container = stack.get(com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents.SPELL_CONTAINER);
        if (container != null && !container.spell_ids().isEmpty()) return ItemUseAnimation.BOW;
        return ItemUseAnimation.NONE;
    }

    public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        if (this.isShield) return 72000;
        var container = stack.get(com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents.SPELL_CONTAINER);
        if (container != null && !container.spell_ids().isEmpty()) return 72000;
        return 0;
    }

    public String getWeaponName() {
        return this.weaponName;
    }

    public float getBaseDamage() {
        return this.baseDamage;
    }

    public List<WeaponSkill> getSkills() {
        return this.skills;
    }

    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!WeaponStatRoller.isWeaponInitialized(stack)) {
            WeaponStatRoller.rollAndApply(stack, this.baseDamage, level.random, this.isShield);
        }
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        WeaponStatRoller.appendWeaponTooltip(stack, tooltip);
        // Phase H: manual "Skills (Right-Click)" and SpellAbilityBridge tooltip blocks removed —
        // tomes/Arsenal/wands now show spell info via SpellEngine's own tooltip renderer.
    }

    public boolean isFoil(ItemStack stack) {
        if (WeaponStatRoller.isWeaponInitialized(stack)) {
            WeaponRarity rarity = WeaponStatRoller.getRarity(stack);
            return rarity == WeaponRarity.MYTHIC || rarity == WeaponRarity.LEGENDARY;
        }
        return false;
    }

    public record WeaponSkill(String name, String description, int cooldownTicks) {
        public float cooldownSeconds() {
            return (float)this.cooldownTicks / 20.0f;
        }
    }
}

