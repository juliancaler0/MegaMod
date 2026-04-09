package com.ultra.megamod.feature.relics.infusion;

import com.ultra.megamod.feature.dungeons.loot.DungeonExclusiveItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Optional;

/**
 * Relic Infusion: combine a dungeon material with a relic/weapon at the Research Table
 * to add a permanent minor bonus. Each relic can only be infused once.
 *
 * Material → Bonus (balanced: small flat values, no percentage scaling):
 *   Cerulean Ingot    → +1.5 Attack Damage
 *   Crystalline Shard → +3% Critical Chance
 *   Spectral Silk     → +2% Dodge Chance
 *   Umbra Ingot       → +2% Lifesteal
 *   Void Shard        → +1.0 Max Health
 *
 * Cost: 500 MegaCoins + 3x material
 */
public class RelicInfusionManager {

    public static final int INFUSION_COIN_COST = 500;
    public static final int MATERIAL_COUNT = 3;
    private static final String INFUSION_TAG = "megamod_infused";
    private static final String INFUSION_MATERIAL_TAG = "megamod_infusion_material";

    public record InfusionRecipe(String materialId, String bonusName, String bonusDescription, String attributeId, double bonusValue) {}

    public static final List<InfusionRecipe> RECIPES = List.of(
        new InfusionRecipe("megamod:cerulean_ingot", "Cerulean Edge", "+1.5 Attack Damage", "minecraft:attack_damage", 1.5),
        new InfusionRecipe("megamod:crystalline_shard", "Crystalline Focus", "+3% Critical Chance", "megamod:critical_chance", 3.0),
        new InfusionRecipe("megamod:spectral_silk", "Spectral Weave", "+2% Dodge Chance", "megamod:dodge_chance", 2.0),
        new InfusionRecipe("megamod:umbra_ingot", "Umbral Drain", "+2% Lifesteal", "megamod:lifesteal", 2.0),
        new InfusionRecipe("megamod:void_shard", "Void Resilience", "+1.0 Max Health", "minecraft:max_health", 1.0)
    );

    /** Check if an item has already been infused. */
    public static boolean isInfused(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        return tag.getBooleanOr(INFUSION_TAG, false);
    }

    /** Get the infusion material used, or empty string if not infused. */
    public static String getInfusionMaterial(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        return tag.getStringOr(INFUSION_MATERIAL_TAG, "");
    }

    /** Find the recipe for a given material ID. */
    public static InfusionRecipe getRecipeForMaterial(String materialId) {
        for (InfusionRecipe recipe : RECIPES) {
            if (recipe.materialId().equals(materialId)) return recipe;
        }
        return null;
    }

    /**
     * Apply an infusion to a relic/weapon.
     * Adds a permanent attribute modifier and marks the item as infused.
     */
    public static boolean applyInfusion(ItemStack stack, InfusionRecipe recipe) {
        if (isInfused(stack)) return false;

        // Mark as infused
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        tag.putBoolean(INFUSION_TAG, true);
        tag.putString(INFUSION_MATERIAL_TAG, recipe.materialId());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        // Add attribute modifier
        try {
            Identifier attrId = Identifier.parse(recipe.attributeId());
            Optional<?> attrOpt = BuiltInRegistries.ATTRIBUTE.get(attrId);
            if (attrOpt.isPresent()) {
                var holder = (net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>) attrOpt.get();
                var modifier = new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    Identifier.fromNamespaceAndPath("megamod", "infusion_" + recipe.bonusName().toLowerCase().replace(' ', '_')),
                    recipe.bonusValue(),
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                );
                var existingMods = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                    net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
                var builder = net.minecraft.world.item.component.ItemAttributeModifiers.builder();
                // Copy existing modifiers via modifiers list
                for (var entry : existingMods.modifiers()) {
                    builder.add(entry.attribute(), entry.modifier(), entry.slot());
                }
                // Add infusion modifier
                builder.add(holder, modifier, net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND);
                stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
            }
        } catch (Exception e) {
            return false;
        }

        // Update lore to show infusion
        var existingLore = stack.getOrDefault(DataComponents.LORE,
            new net.minecraft.world.item.component.ItemLore(List.of()));
        var newLines = new java.util.ArrayList<>(existingLore.lines());
        newLines.add(Component.empty());
        newLines.add(Component.literal("Infused: " + recipe.bonusName()).withStyle(
            Style.EMPTY.withColor(0xAA55FF).withItalic(false)));
        newLines.add(Component.literal("  " + recipe.bonusDescription()).withStyle(
            Style.EMPTY.withColor(0x8844CC).withItalic(false)));
        stack.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(newLines));

        return true;
    }
}
