/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.EquipmentSlotGroup
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.item.component.ItemAttributeModifiers
 *  net.minecraft.world.item.component.ItemAttributeModifiers$Builder
 */
package com.ultra.megamod.feature.relics.data;

import com.ultra.megamod.feature.relics.data.WeaponRarity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class WeaponStatRoller {
    public static final String KEY_WEAPON_RARITY = "weapon_rarity";
    public static final String KEY_WEAPON_INITIALIZED = "weapon_stats_initialized";
    public static final String KEY_WEAPON_BASE_DAMAGE = "weapon_base_damage";
    public static final String KEY_ROLLED_BONUSES = "weapon_rolled_bonuses";
    public static final String KEY_WEAPON_IS_SHIELD = "weapon_is_shield";
    public static final List<BonusStat> BONUS_POOL = List.of(new BonusStat("minecraft:attack_damage", 0.5, 3.0, AttributeModifier.Operation.ADD_VALUE, "Attack Damage", false), new BonusStat("minecraft:attack_speed", 0.05, 0.3, AttributeModifier.Operation.ADD_VALUE, "Attack Speed", false), new BonusStat("minecraft:max_health", 1.0, 6.0, AttributeModifier.Operation.ADD_VALUE, "Max Health", false), new BonusStat("minecraft:movement_speed", 0.01, 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, "Movement Speed", true), new BonusStat("minecraft:armor", 1.0, 4.0, AttributeModifier.Operation.ADD_VALUE, "Armor", false), new BonusStat("minecraft:armor_toughness", 0.5, 2.0, AttributeModifier.Operation.ADD_VALUE, "Armor Toughness", false), new BonusStat("minecraft:luck", 0.5, 2.0, AttributeModifier.Operation.ADD_VALUE, "Luck", false), new BonusStat("megamod:critical_chance", 2.0, 15.0, AttributeModifier.Operation.ADD_VALUE, "Critical Chance", true), new BonusStat("megamod:critical_damage", 5.0, 30.0, AttributeModifier.Operation.ADD_VALUE, "Critical Damage", true), new BonusStat("megamod:lifesteal", 1.0, 8.0, AttributeModifier.Operation.ADD_VALUE, "Lifesteal", true), new BonusStat("megamod:fire_damage_bonus", 2.0, 10.0, AttributeModifier.Operation.ADD_VALUE, "Fire Damage", false), new BonusStat("megamod:ice_damage_bonus", 2.0, 10.0, AttributeModifier.Operation.ADD_VALUE, "Ice Damage", false), new BonusStat("megamod:lightning_damage_bonus", 2.0, 10.0, AttributeModifier.Operation.ADD_VALUE, "Lightning Damage", false), new BonusStat("megamod:cooldown_reduction", 2.0, 15.0, AttributeModifier.Operation.ADD_VALUE, "Cooldown Reduction", true), new BonusStat("megamod:dodge_chance", 1.0, 5.0, AttributeModifier.Operation.ADD_VALUE, "Dodge Chance", true), new BonusStat("megamod:health_regen_bonus", 0.5, 2.0, AttributeModifier.Operation.ADD_VALUE, "Health Regen", false));

    public static boolean isWeaponInitialized(ItemStack stack) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return tag.getBooleanOr(KEY_WEAPON_INITIALIZED, false);
    }

    public static WeaponRarity getRarity(ItemStack stack) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return WeaponRarity.fromOrdinal(tag.getIntOr(KEY_WEAPON_RARITY, 0));
    }

    public static float getStoredBaseDamage(ItemStack stack) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return tag.getFloatOr(KEY_WEAPON_BASE_DAMAGE, 0.0f);
    }

    public static void rollAndApply(ItemStack stack, float baseDamage, RandomSource random) {
        WeaponRarity rarity = WeaponRarity.roll(random);
        WeaponStatRoller.rollAndApply(stack, baseDamage, rarity, random, false);
    }

    public static void rollAndApply(ItemStack stack, float baseDamage, WeaponRarity rarity, RandomSource random) {
        rollAndApply(stack, baseDamage, rarity, random, false);
    }

    public static void rollAndApply(ItemStack stack, float baseDamage, RandomSource random, boolean isShield) {
        WeaponRarity rarity = WeaponRarity.roll(random);
        rollAndApply(stack, baseDamage, rarity, random, isShield);
    }

    public static void rollAndApply(ItemStack stack, float baseDamage, WeaponRarity rarity, RandomSource random, boolean isShield) {
        // Shields use ANY slot group for bonuses so stats apply from offhand too
        EquipmentSlotGroup bonusGroup = isShield ? EquipmentSlotGroup.ANY : EquipmentSlotGroup.MAINHAND;
        float finalDamage = baseDamage * rarity.getDamageMultiplier();
        int bonusCount = rarity.rollBonusCount(random);
        ArrayList<BonusStat> available = new ArrayList<BonusStat>(BONUS_POOL);
        Collections.shuffle(available, new Random(random.nextLong()));
        ItemAttributeModifiers.Builder modBuilder = ItemAttributeModifiers.builder();
        Optional<Holder<Attribute>> attackDmgAttr = WeaponStatRoller.resolveAttribute("minecraft:attack_damage");
        if (attackDmgAttr.isPresent()) {
            AttributeModifier baseDmgMod = new AttributeModifier(Identifier.fromNamespaceAndPath("megamod",KEY_WEAPON_BASE_DAMAGE), (double)finalDamage - 1.0, AttributeModifier.Operation.ADD_VALUE);
            modBuilder.add(attackDmgAttr.get(), baseDmgMod, EquipmentSlotGroup.MAINHAND);
        }
        // Apply BetterCombat-style base attack_speed so each weapon type has the right swing cadence
        // (vanilla default is 4.0 which maps to 5-tick cooldowns — way too fast for heavy weapons).
        if (!isShield) {
            Optional<Holder<Attribute>> attackSpeedAttr = WeaponStatRoller.resolveAttribute("minecraft:attack_speed");
            if (attackSpeedAttr.isPresent()) {
                String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(stack.getItem()).toString();
                double targetSpeed = WeaponCategorySpeed.getTargetAttackSpeed(itemId);
                double delta = WeaponCategorySpeed.toModifierDelta(targetSpeed);
                AttributeModifier baseSpeedMod = new AttributeModifier(
                        Identifier.fromNamespaceAndPath("megamod", "weapon_base_attack_speed"),
                        delta, AttributeModifier.Operation.ADD_VALUE);
                modBuilder.add(attackSpeedAttr.get(), baseSpeedMod, EquipmentSlotGroup.MAINHAND);
            }
        }
        CompoundTag bonusesTag = new CompoundTag();
        int applied = 0;
        for (int i = 0; i < available.size() && applied < bonusCount; ++i) {
            BonusStat bonus = (BonusStat)available.get(i);
            Optional<Holder<Attribute>> attrHolder = WeaponStatRoller.resolveAttribute(bonus.attributeId());
            if (attrHolder.isEmpty()) continue;
            double value = bonus.roll(random, rarity);
            AttributeModifier modifier = new AttributeModifier(Identifier.fromNamespaceAndPath("megamod",("weapon_bonus_" + applied + "_" + random.nextInt(10000))), value, bonus.operation());
            modBuilder.add(attrHolder.get(), modifier, bonusGroup);
            CompoundTag bonusEntry = new CompoundTag();
            bonusEntry.putString("name", bonus.displayName());
            bonusEntry.putString("attr", bonus.attributeId());
            bonusEntry.putDouble("value", value);
            bonusEntry.putBoolean("percent", bonus.isPercent());
            bonusEntry.putInt("op", bonus.operation().ordinal());
            bonusesTag.put("bonus_" + applied, bonusEntry);
            ++applied;
        }
        bonusesTag.putInt("count", applied);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modBuilder.build());
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        tag.putBoolean(KEY_WEAPON_INITIALIZED, true);
        tag.putInt(KEY_WEAPON_RARITY, rarity.ordinal());
        tag.putFloat(KEY_WEAPON_BASE_DAMAGE, finalDamage);
        tag.putBoolean(KEY_WEAPON_IS_SHIELD, isShield);
        tag.put(KEY_ROLLED_BONUSES, bonusesTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        String baseName = Component.translatable((String)stack.getItem().getDescriptionId()).getString();
        String displayName = rarity.getDisplayName() + " " + baseName;
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(displayName).withStyle(rarity.getNameColor()));
    }

    public static void rerollBonuses(ItemStack stack, RandomSource random) {
        if (!isWeaponInitialized(stack)) return;
        WeaponRarity rarity = getRarity(stack);
        float baseDamage = getStoredBaseDamage(stack);
        boolean isShield = isStoredShield(stack);
        EquipmentSlotGroup bonusGroup = isShield ? EquipmentSlotGroup.ANY : EquipmentSlotGroup.MAINHAND;
        int bonusCount = rarity.rollBonusCount(random);
        ArrayList<BonusStat> available = new ArrayList<BonusStat>(BONUS_POOL);
        Collections.shuffle(available, new Random(random.nextLong()));
        ItemAttributeModifiers.Builder modBuilder = ItemAttributeModifiers.builder();
        Optional<Holder<Attribute>> attackDmgAttr = resolveAttribute("minecraft:attack_damage");
        if (attackDmgAttr.isPresent()) {
            AttributeModifier baseDmgMod = new AttributeModifier(Identifier.fromNamespaceAndPath("megamod", KEY_WEAPON_BASE_DAMAGE), (double) baseDamage - 1.0, AttributeModifier.Operation.ADD_VALUE);
            modBuilder.add(attackDmgAttr.get(), baseDmgMod, EquipmentSlotGroup.MAINHAND);
        }
        // Reapply base attack_speed on reroll so the weapon keeps its category's swing cadence
        if (!isShield) {
            Optional<Holder<Attribute>> attackSpeedAttr = resolveAttribute("minecraft:attack_speed");
            if (attackSpeedAttr.isPresent()) {
                String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(stack.getItem()).toString();
                double targetSpeed = WeaponCategorySpeed.getTargetAttackSpeed(itemId);
                double delta = WeaponCategorySpeed.toModifierDelta(targetSpeed);
                AttributeModifier baseSpeedMod = new AttributeModifier(
                        Identifier.fromNamespaceAndPath("megamod", "weapon_base_attack_speed"),
                        delta, AttributeModifier.Operation.ADD_VALUE);
                modBuilder.add(attackSpeedAttr.get(), baseSpeedMod, EquipmentSlotGroup.MAINHAND);
            }
        }
        CompoundTag bonusesTag = new CompoundTag();
        int applied = 0;
        for (int i = 0; i < available.size() && applied < bonusCount; ++i) {
            BonusStat bonus = available.get(i);
            Optional<Holder<Attribute>> attrHolder = resolveAttribute(bonus.attributeId());
            if (attrHolder.isEmpty()) continue;
            double value = bonus.roll(random, rarity);
            AttributeModifier modifier = new AttributeModifier(Identifier.fromNamespaceAndPath("megamod", "weapon_bonus_" + applied + "_" + random.nextInt(10000)), value, bonus.operation());
            modBuilder.add(attrHolder.get(), modifier, bonusGroup);
            CompoundTag bonusEntry = new CompoundTag();
            bonusEntry.putString("name", bonus.displayName());
            bonusEntry.putString("attr", bonus.attributeId());
            bonusEntry.putDouble("value", value);
            bonusEntry.putBoolean("percent", bonus.isPercent());
            bonusEntry.putInt("op", bonus.operation().ordinal());
            bonusesTag.put("bonus_" + applied, bonusEntry);
            ++applied;
        }
        bonusesTag.putInt("count", applied);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modBuilder.build());
        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        tag.put(KEY_ROLLED_BONUSES, bonusesTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean isStoredShield(ItemStack stack) {
        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return tag.getBooleanOr(KEY_WEAPON_IS_SHIELD, false);
    }

    public static void rebuildModifiersFromTag(ItemStack stack) {
        if (!isWeaponInitialized(stack)) return;
        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        float baseDamage = tag.getFloatOr(KEY_WEAPON_BASE_DAMAGE, 0.0f);
        boolean isShield = tag.getBooleanOr(KEY_WEAPON_IS_SHIELD, false);
        EquipmentSlotGroup bonusGroup = isShield ? EquipmentSlotGroup.ANY : EquipmentSlotGroup.MAINHAND;
        CompoundTag bonusesTag = tag.getCompoundOrEmpty(KEY_ROLLED_BONUSES);
        int count = bonusesTag.getIntOr("count", 0);

        ItemAttributeModifiers.Builder modBuilder = ItemAttributeModifiers.builder();
        Optional<Holder<Attribute>> attackDmgAttr = resolveAttribute("minecraft:attack_damage");
        if (attackDmgAttr.isPresent()) {
            AttributeModifier baseDmgMod = new AttributeModifier(
                Identifier.fromNamespaceAndPath("megamod", KEY_WEAPON_BASE_DAMAGE),
                (double) baseDamage - 1.0, AttributeModifier.Operation.ADD_VALUE);
            modBuilder.add(attackDmgAttr.get(), baseDmgMod, EquipmentSlotGroup.MAINHAND);
        }

        for (int i = 0; i < count; i++) {
            CompoundTag entry = bonusesTag.getCompoundOrEmpty("bonus_" + i);
            String attrId = entry.getStringOr("attr", "");
            double value = entry.getDoubleOr("value", 0.0);
            int opOrd = entry.getIntOr("op", 0);
            if (attrId.isEmpty()) continue;
            Optional<Holder<Attribute>> attrHolder = resolveAttribute(attrId);
            if (attrHolder.isEmpty()) continue;
            AttributeModifier.Operation op = AttributeModifier.Operation.values()[Math.min(opOrd, AttributeModifier.Operation.values().length - 1)];
            AttributeModifier modifier = new AttributeModifier(
                Identifier.fromNamespaceAndPath("megamod", "weapon_bonus_" + i + "_rebuilt"),
                value, op);
            modBuilder.add(attrHolder.get(), modifier, bonusGroup);
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modBuilder.build());
    }

    public static void appendWeaponTooltip(ItemStack stack, Consumer<Component> tooltip) {
        if (!WeaponStatRoller.isWeaponInitialized(stack)) {
            tooltip.accept(Component.literal("Unrolled - enter inventory to initialize").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
            return;
        }
        WeaponRarity rarity = WeaponStatRoller.getRarity(stack);
        float baseDamage = WeaponStatRoller.getStoredBaseDamage(stack);
        tooltip.accept(Component.literal("Rarity: ").withStyle(ChatFormatting.GRAY).append(Component.literal(rarity.getDisplayName()).withStyle(rarity.getNameColor())));
        tooltip.accept(Component.literal(String.format("Base Damage: %.1f", Float.valueOf(baseDamage))).withStyle(ChatFormatting.DARK_GREEN));
        // DPS calculation
        float attackSpeed = 1.6f; // Default sword speed (4.0 base + typical -2.4 modifier)
        ItemAttributeModifiers mods = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (var entry : mods.modifiers()) {
            if (entry.attribute().is(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED)
                && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                attackSpeed = (float)(4.0 + entry.modifier().amount());
                break;
            }
        }
        float dps = baseDamage * Math.max(0.1f, attackSpeed);
        tooltip.accept(Component.literal(String.format("DPS: %.1f", dps)).withStyle(ChatFormatting.DARK_AQUA));
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag bonusesTag = tag.getCompoundOrEmpty(KEY_ROLLED_BONUSES);
        int count = bonusesTag.getIntOr("count", 0);
        if (count > 0) {
            tooltip.accept(Component.empty());
            tooltip.accept(Component.literal("Bonus Stats:").withStyle(ChatFormatting.AQUA));
            for (int i = 0; i < count; ++i) {
                String sign;
                CompoundTag entry = bonusesTag.getCompoundOrEmpty("bonus_" + i);
                String name = entry.getStringOr("name", "Unknown");
                double value = entry.getDoubleOr("value", 0.0);
                boolean isPercent = entry.getBooleanOr("percent", false);
                int opOrdinal = entry.getIntOr("op", 0);
                String string = sign = value >= 0.0 ? "+" : "";
                String formatted = isPercent || opOrdinal == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.ordinal() ? sign + String.format("%.1f%%", value * (opOrdinal == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.ordinal() ? 100.0 : 1.0)) : sign + String.format("%.1f", value);
                tooltip.accept(Component.literal(("  " + name + ": " + formatted)).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private static Optional<Holder<Attribute>> resolveAttribute(String attributeId) {
        try {
            Identifier id = Identifier.parse(attributeId);
            var ref = BuiltInRegistries.ATTRIBUTE.get(id);
            if (ref.isPresent()) {
                return Optional.of(ref.get());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return Optional.empty();
    }

    public record BonusStat(String attributeId, double minValue, double maxValue, AttributeModifier.Operation operation, String displayName, boolean isPercent) {
        public double roll(RandomSource random, WeaponRarity rarity) {
            double base = this.minValue + (this.maxValue - this.minValue) * random.nextDouble();
            double rarityScale = 1.0 + (double)rarity.ordinal() * 0.125;
            return base * rarityScale;
        }
    }
}

