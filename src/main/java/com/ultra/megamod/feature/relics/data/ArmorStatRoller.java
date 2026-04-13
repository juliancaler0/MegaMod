package com.ultra.megamod.feature.relics.data;

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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Stat roller for dungeon armor pieces. Works like WeaponStatRoller but for armor.
 * Rolls rarity, scales base armor/toughness, and adds random bonus attributes.
 */
public class ArmorStatRoller {
    public static final String KEY_ARMOR_INITIALIZED = "armor_stats_initialized";
    public static final String KEY_ARMOR_RARITY = "armor_rarity";
    public static final String KEY_ARMOR_BASE = "armor_base_value";
    public static final String KEY_ARMOR_TOUGHNESS_BASE = "armor_toughness_value";
    public static final String KEY_ROLLED_BONUSES = "armor_rolled_bonuses";

    public static final List<WeaponStatRoller.BonusStat> ARMOR_BONUS_POOL = List.of(
        new WeaponStatRoller.BonusStat("minecraft:max_health", 1.0, 8.0, AttributeModifier.Operation.ADD_VALUE, "Max Health", false),
        new WeaponStatRoller.BonusStat("minecraft:armor", 0.5, 3.0, AttributeModifier.Operation.ADD_VALUE, "Armor", false),
        new WeaponStatRoller.BonusStat("minecraft:armor_toughness", 0.5, 2.0, AttributeModifier.Operation.ADD_VALUE, "Armor Toughness", false),
        new WeaponStatRoller.BonusStat("minecraft:movement_speed", 0.005, 0.02, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, "Movement Speed", true),
        new WeaponStatRoller.BonusStat("minecraft:knockback_resistance", 0.02, 0.1, AttributeModifier.Operation.ADD_VALUE, "KB Resistance", true),
        new WeaponStatRoller.BonusStat("megamod:critical_chance", 1.0, 8.0, AttributeModifier.Operation.ADD_VALUE, "Critical Chance", true),
        new WeaponStatRoller.BonusStat("megamod:dodge_chance", 1.0, 5.0, AttributeModifier.Operation.ADD_VALUE, "Dodge Chance", true),
        new WeaponStatRoller.BonusStat("megamod:health_regen_bonus", 0.3, 1.5, AttributeModifier.Operation.ADD_VALUE, "Health Regen", false),
        new WeaponStatRoller.BonusStat("megamod:fire_resistance_bonus", 2.0, 10.0, AttributeModifier.Operation.ADD_VALUE, "Fire Resist", true),
        new WeaponStatRoller.BonusStat("megamod:ice_resistance_bonus", 2.0, 10.0, AttributeModifier.Operation.ADD_VALUE, "Ice Resist", true),
        new WeaponStatRoller.BonusStat("megamod:lightning_resistance_bonus", 2.0, 10.0, AttributeModifier.Operation.ADD_VALUE, "Lightning Resist", true),
        new WeaponStatRoller.BonusStat("megamod:poison_resistance_bonus", 2.0, 10.0, AttributeModifier.Operation.ADD_VALUE, "Poison Resist", true),
        new WeaponStatRoller.BonusStat("megamod:thorns_damage", 1.0, 5.0, AttributeModifier.Operation.ADD_VALUE, "Thorns Damage", false),
        new WeaponStatRoller.BonusStat("megamod:fall_damage_reduction", 5.0, 25.0, AttributeModifier.Operation.ADD_VALUE, "Fall Reduction", true),
        new WeaponStatRoller.BonusStat("megamod:lifesteal", 0.5, 3.0, AttributeModifier.Operation.ADD_VALUE, "Lifesteal", true),
        new WeaponStatRoller.BonusStat("megamod:cooldown_reduction", 1.0, 8.0, AttributeModifier.Operation.ADD_VALUE, "Cooldown Reduction", true)
    );

    public static boolean isArmorInitialized(ItemStack stack) {
        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return tag.getBooleanOr(KEY_ARMOR_INITIALIZED, false);
    }

    public static WeaponRarity getRarity(ItemStack stack) {
        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return WeaponRarity.fromOrdinal(tag.getIntOr(KEY_ARMOR_RARITY, 0));
    }

    public static void rollAndApply(ItemStack stack, double baseArmor, double baseToughness,
                                     EquipmentSlot slot, RandomSource random) {
        WeaponRarity rarity = WeaponRarity.roll(random);
        rollAndApply(stack, baseArmor, baseToughness, slot, rarity, random);
    }

    public static void rollAndApply(ItemStack stack, double baseArmor, double baseToughness,
                                     EquipmentSlot slot, WeaponRarity rarity, RandomSource random) {
        double armorMult = 1.0 + (rarity.ordinal() * 0.15);
        double finalArmor = baseArmor * armorMult;
        double finalToughness = baseToughness * armorMult;
        // Guarantee at least 1 bonus stat so even Common feels rewarding
        int bonusCount = Math.max(1, rarity.rollBonusCount(random));

        EquipmentSlotGroup group = switch (slot) {
            case HEAD -> EquipmentSlotGroup.HEAD;
            case CHEST -> EquipmentSlotGroup.CHEST;
            case LEGS -> EquipmentSlotGroup.LEGS;
            case FEET -> EquipmentSlotGroup.FEET;
            default -> EquipmentSlotGroup.ARMOR;
        };

        ItemAttributeModifiers.Builder modBuilder = ItemAttributeModifiers.builder();

        // Slot-specific modifier suffix so multiple armor pieces worn together don't collide.
        // AttributeMap keys modifiers by Identifier — matching IDs overwrite each other.
        String slotSuffix = slot.getName();

        // Base armor — use direct Attributes reference (string resolution can silently fail)
        modBuilder.add(Attributes.ARMOR, new AttributeModifier(
            Identifier.fromNamespaceAndPath("megamod", "armor_base_" + slotSuffix),
            finalArmor, AttributeModifier.Operation.ADD_VALUE), group);

        // Base toughness
        if (finalToughness > 0) {
            modBuilder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                Identifier.fromNamespaceAndPath("megamod", "armor_toughness_base_" + slotSuffix),
                finalToughness, AttributeModifier.Operation.ADD_VALUE), group);
        }

        // Base knockback resistance (scales with rarity like vanilla netherite)
        double kbResist = rarity.ordinal() * 0.025; // 0, 0.025, 0.05, 0.075, 0.1
        if (kbResist > 0) {
            modBuilder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(
                Identifier.fromNamespaceAndPath("megamod", "armor_kb_resist_base_" + slotSuffix),
                kbResist, AttributeModifier.Operation.ADD_VALUE), group);
        }

        // Roll bonus stats
        ArrayList<WeaponStatRoller.BonusStat> available = new ArrayList<>(ARMOR_BONUS_POOL);
        Collections.shuffle(available, new Random(random.nextLong()));

        CompoundTag bonusesTag = new CompoundTag();
        int applied = 0;
        for (int i = 0; i < available.size() && applied < bonusCount; i++) {
            WeaponStatRoller.BonusStat bonus = available.get(i);
            // Skip armor/toughness/KB resist from bonus pool since we already have base values
            if (bonus.attributeId().equals("minecraft:armor") || bonus.attributeId().equals("minecraft:armor_toughness")
                || bonus.attributeId().equals("minecraft:knockback_resistance"))
                continue;
            Optional<Holder<Attribute>> attrHolder = resolveAttribute(bonus.attributeId());
            if (attrHolder.isEmpty()) continue;

            double value = bonus.roll(random, rarity);
            AttributeModifier modifier = new AttributeModifier(
                Identifier.fromNamespaceAndPath("megamod", "armor_bonus_" + slotSuffix + "_" + applied + "_" + random.nextInt(10000)),
                value, bonus.operation());
            modBuilder.add(attrHolder.get(), modifier, group);

            CompoundTag bonusEntry = new CompoundTag();
            bonusEntry.putString("name", bonus.displayName());
            bonusEntry.putString("attr", bonus.attributeId());
            bonusEntry.putDouble("value", value);
            bonusEntry.putBoolean("percent", bonus.isPercent());
            bonusEntry.putInt("op", bonus.operation().ordinal());
            bonusesTag.put("bonus_" + applied, bonusEntry);
            applied++;
        }
        bonusesTag.putInt("count", applied);

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modBuilder.build());

        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        tag.putBoolean(KEY_ARMOR_INITIALIZED, true);
        tag.putInt(KEY_ARMOR_RARITY, rarity.ordinal());
        tag.putDouble(KEY_ARMOR_BASE, finalArmor);
        tag.putDouble(KEY_ARMOR_TOUGHNESS_BASE, finalToughness);
        tag.put(KEY_ROLLED_BONUSES, bonusesTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        // Set display name with rarity color
        String baseName = Component.translatable(stack.getItem().getDescriptionId()).getString();
        String displayName = rarity.getDisplayName() + " " + baseName;
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(displayName).withStyle(rarity.getNameColor()));
    }

    public static double getStoredBaseArmor(ItemStack stack) {
        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return tag.getDoubleOr(KEY_ARMOR_BASE, 0.0);
    }

    public static double getStoredBaseToughness(ItemStack stack) {
        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return tag.getDoubleOr(KEY_ARMOR_TOUGHNESS_BASE, 0.0);
    }

    public static void rerollBonuses(ItemStack stack, RandomSource random) {
        if (!isArmorInitialized(stack)) return;
        WeaponRarity rarity = getRarity(stack);
        double storedArmor = getStoredBaseArmor(stack);
        double storedToughness = getStoredBaseToughness(stack);
        // Determine equipment slot from the item
        EquipmentSlot slot = EquipmentSlot.CHEST;
        net.minecraft.world.item.equipment.Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null) slot = equippable.slot();
        // Reverse the rarity multiplier to get original base values
        double mult = 1.0 + rarity.ordinal() * 0.15;
        rollAndApply(stack, storedArmor / mult, storedToughness / mult, slot, rarity, random);
    }

    public static void appendArmorTooltip(ItemStack stack, Consumer<Component> tooltip) {
        if (!isArmorInitialized(stack)) {
            tooltip.accept(Component.literal("Unrolled - enter inventory to initialize")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return;
        }
        WeaponRarity rarity = getRarity(stack);
        tooltip.accept(Component.literal("Rarity: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(rarity.getDisplayName()).withStyle(rarity.getNameColor())));

        // Show base armor stats
        double baseArmor = getStoredBaseArmor(stack);
        double baseToughness = getStoredBaseToughness(stack);
        tooltip.accept(Component.literal(String.format("Armor: %.1f", baseArmor)).withStyle(ChatFormatting.DARK_GREEN));
        if (baseToughness > 0) {
            tooltip.accept(Component.literal(String.format("Toughness: %.1f", baseToughness)).withStyle(ChatFormatting.DARK_GREEN));
        }

        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag bonusesTag = tag.getCompoundOrEmpty(KEY_ROLLED_BONUSES);
        int count = bonusesTag.getIntOr("count", 0);
        if (count > 0) {
            tooltip.accept(Component.empty());
            tooltip.accept(Component.literal("Bonus Stats:").withStyle(ChatFormatting.AQUA));
            for (int i = 0; i < count; i++) {
                CompoundTag entry = bonusesTag.getCompoundOrEmpty("bonus_" + i);
                String name = entry.getStringOr("name", "Unknown");
                double value = entry.getDoubleOr("value", 0.0);
                boolean isPercent = entry.getBooleanOr("percent", false);
                int opOrdinal = entry.getIntOr("op", 0);
                String sign = value >= 0 ? "+" : "";
                String formatted = isPercent || opOrdinal == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.ordinal()
                    ? sign + String.format("%.1f%%", value * (opOrdinal == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.ordinal() ? 100.0 : 1.0))
                    : sign + String.format("%.1f", value);
                tooltip.accept(Component.literal("  " + name + ": " + formatted).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private static Optional<Holder<Attribute>> resolveAttribute(String attributeId) {
        try {
            Identifier id = Identifier.parse(attributeId);
            var ref = BuiltInRegistries.ATTRIBUTE.get(id);
            if (ref.isPresent()) return Optional.of(ref.get());
        } catch (Exception ignored) {}
        return Optional.empty();
    }
}
