package com.ultra.megamod.feature.relics.weapons;

import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.feature.relics.data.WeaponRarity;
import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import com.ultra.megamod.lib.accessories.api.attributes.AccessoryAttributeBuilder;
import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Jewelry item with rolled rarity and random bonus attributes.
 * Keeps the base attributes set at registration (e.g., +4% arcane power for Topaz Ring)
 * and adds 1-3 random bonus stats from a jewelry-specific pool on first inventory tick.
 * <p>
 * Extends {@link com.ultra.megamod.lib.accessories.api.core.AccessoryItem} so each instance
 * auto-registers with the lib/accessories system. Slot membership is driven by the
 * {@code megamod:rings}/{@code megamod:necklaces} item tags, which are chained into the
 * {@code accessories:ring}/{@code accessories:necklace} validator tags.
 */
public class RpgJewelryItem extends com.ultra.megamod.lib.accessories.api.core.AccessoryItem {
    private static final String KEY_JEWELRY_INITIALIZED = "jewelry_stats_initialized";
    private static final String KEY_JEWELRY_RARITY = "jewelry_rarity";
    private static final String KEY_JEWELRY_BONUSES = "jewelry_rolled_bonuses";

    private final AccessorySlotType slotType;

    /** Jewelry-specific bonus pool — utility/defensive stats appropriate for accessories. */
    private static final List<WeaponStatRoller.BonusStat> JEWELRY_BONUS_POOL = List.of(
            new WeaponStatRoller.BonusStat("minecraft:max_health", 1.0, 4.0, AttributeModifier.Operation.ADD_VALUE, "Max Health", false),
            new WeaponStatRoller.BonusStat("minecraft:movement_speed", 0.005, 0.015, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, "Movement Speed", true),
            new WeaponStatRoller.BonusStat("minecraft:armor", 0.5, 2.0, AttributeModifier.Operation.ADD_VALUE, "Armor", false),
            new WeaponStatRoller.BonusStat("megamod:critical_chance", 1.0, 6.0, AttributeModifier.Operation.ADD_VALUE, "Critical Chance", true),
            new WeaponStatRoller.BonusStat("megamod:critical_damage", 2.0, 12.0, AttributeModifier.Operation.ADD_VALUE, "Critical Damage", true),
            new WeaponStatRoller.BonusStat("megamod:spell_haste", 1.0, 5.0, AttributeModifier.Operation.ADD_VALUE, "Spell Haste", true),
            new WeaponStatRoller.BonusStat("megamod:cooldown_reduction", 1.0, 6.0, AttributeModifier.Operation.ADD_VALUE, "Cooldown Reduction", true),
            new WeaponStatRoller.BonusStat("megamod:lifesteal", 0.5, 3.0, AttributeModifier.Operation.ADD_VALUE, "Lifesteal", true),
            new WeaponStatRoller.BonusStat("megamod:health_regen_bonus", 0.2, 1.0, AttributeModifier.Operation.ADD_VALUE, "Health Regen", false),
            new WeaponStatRoller.BonusStat("megamod:dodge_chance", 0.5, 3.0, AttributeModifier.Operation.ADD_VALUE, "Dodge Chance", true),
            new WeaponStatRoller.BonusStat("minecraft:luck", 0.5, 2.0, AttributeModifier.Operation.ADD_VALUE, "Luck", false)
    );

    public RpgJewelryItem(Item.Properties properties, AccessorySlotType slotType) {
        super(properties);
        this.slotType = slotType;
    }

    public AccessorySlotType getSlotType() {
        return slotType;
    }

    /**
     * Bridges vanilla {@link DataComponents#ATTRIBUTE_MODIFIERS} into the lib's
     * accessory attribute system. Without this, rings/necklaces equipped in
     * lib/accessories slots don't apply any of their rolled stats — vanilla only
     * consults ATTRIBUTE_MODIFIERS when an item is in a native equipment slot.
     */
    @Override
    public void getDynamicModifiers(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        super.getDynamicModifiers(stack, reference, builder);

        // If a stack reaches the equip path without having rolled yet (e.g. it
        // went straight from /give into an accessory slot and inventoryTick
        // hasn't fired yet), roll eagerly so the attributes we bridge below
        // aren't empty on first equip.
        if (!isInitialized(stack) && reference.entity() != null
                && !reference.entity().level().isClientSide()) {
            rollAndApply(stack, reference.entity().level().random);
        }

        var mods = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (mods == null) return;

        for (var entry : mods.modifiers()) {
            AttributeModifier mod = entry.modifier();
            builder.addStackable(entry.attribute(), mod.id(), mod.amount(), mod.operation());
        }
    }

    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!isInitialized(stack)) {
            rollAndApply(stack, level.random);

            // If this stack is currently in one of the entity's accessory slots,
            // reset that slot's previousItem to EMPTY. rollAndApply mutates
            // DataComponents.ATTRIBUTE_MODIFIERS, which the accessories mutation
            // subscription copies into previousItem — this makes currentStack
            // match previousItem so AccessoriesEventHandler never enters the
            // equip branch and the rolled modifiers are never applied to the
            // player. Clearing previousItem forces the equip branch to run next
            // tick and the attributes finally reach the living entity's
            // attribute map.
            if (entity instanceof net.minecraft.world.entity.LivingEntity living
                    && !level.isClientSide()) {
                var capability = com.ultra.megamod.lib.accessories.api.AccessoriesCapability.get(living);
                if (capability != null) {
                    for (var container : capability.getContainers().values()) {
                        var accessoriesCont = container.getAccessories();
                        for (int i = 0; i < accessoriesCont.getContainerSize(); i++) {
                            if (accessoriesCont.getItem(i) == stack) {
                                accessoriesCont.setPreviousItem(i, ItemStack.EMPTY);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isInitialized(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getBooleanOr(KEY_JEWELRY_INITIALIZED, false);
    }

    private static WeaponRarity getRarity(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return WeaponRarity.fromOrdinal(tag.getIntOr(KEY_JEWELRY_RARITY, 0));
    }

    private void rollAndApply(ItemStack stack, RandomSource random) {
        WeaponRarity rarity = WeaponRarity.roll(random);
        int bonusCount = Math.max(1, rarity.rollBonusCount(random));
        // Jewelry gets fewer bonuses than weapons (cap at 3)
        bonusCount = Math.min(bonusCount, 3);

        // Read existing base attributes (set at registration time)
        ItemAttributeModifiers existingMods = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder modBuilder = ItemAttributeModifiers.builder();

        // Preserve all base attribute modifiers
        for (var entry : existingMods.modifiers()) {
            modBuilder.add(entry.attribute(), entry.modifier(), entry.slot());
        }

        // Roll and add bonus stats
        ArrayList<WeaponStatRoller.BonusStat> available = new ArrayList<>(JEWELRY_BONUS_POOL);
        Collections.shuffle(available, new Random(random.nextLong()));

        CompoundTag bonusesTag = new CompoundTag();
        int applied = 0;
        for (int i = 0; i < available.size() && applied < bonusCount; i++) {
            WeaponStatRoller.BonusStat bonus = available.get(i);
            Optional<Holder<Attribute>> attrHolder = resolveAttribute(bonus.attributeId());
            if (attrHolder.isEmpty()) continue;

            double value = bonus.roll(random, rarity);
            AttributeModifier modifier = new AttributeModifier(
                    Identifier.fromNamespaceAndPath("megamod", "jewelry_bonus_" + applied + "_" + random.nextInt(10000)),
                    value, bonus.operation());
            modBuilder.add(attrHolder.get(), modifier, EquipmentSlotGroup.ANY);

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

        // Store roll metadata
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean(KEY_JEWELRY_INITIALIZED, true);
        tag.putInt(KEY_JEWELRY_RARITY, rarity.ordinal());
        tag.put(KEY_JEWELRY_BONUSES, bonusesTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        // Set rarity-colored display name
        String baseName = Component.translatable(stack.getItem().getDescriptionId()).getString();
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(rarity.getDisplayName() + " " + baseName).withStyle(rarity.getNameColor()));
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                 Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        if (!isInitialized(stack)) {
            tooltip.accept(Component.literal("Unrolled - enter inventory to initialize")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return;
        }

        WeaponRarity rarity = getRarity(stack);
        tooltip.accept(Component.literal("Rarity: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(rarity.getDisplayName()).withStyle(rarity.getNameColor())));
        tooltip.accept(Component.literal("Slot: " + slotType.name().replace("_", " "))
                .withStyle(ChatFormatting.DARK_GRAY));

        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag bonusesTag = tag.getCompoundOrEmpty(KEY_JEWELRY_BONUSES);
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

    public boolean isFoil(ItemStack stack) {
        if (isInitialized(stack)) {
            WeaponRarity rarity = getRarity(stack);
            return rarity == WeaponRarity.MYTHIC || rarity == WeaponRarity.LEGENDARY;
        }
        return false;
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
