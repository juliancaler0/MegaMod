package com.ultra.megamod.feature.tooltips;

import com.ultra.megamod.MegaMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Shows stat comparison when hovering items in any container screen.
 * Green ▲ for upgrades, red ▼ for downgrades compared to currently equipped item.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class ItemComparisonTooltip {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) return;

        ItemStack hovered = event.getItemStack();
        if (hovered.isEmpty()) return;

        // Find the comparison target (currently equipped in same slot)
        ItemStack equipped = findEquippedComparison(hovered);
        if (equipped.isEmpty() || equipped == hovered) return;
        // Don't compare identical items
        if (ItemStack.isSameItemSameComponents(hovered, equipped)) return;

        // Extract attribute modifiers from both items
        Map<String, Double> hoveredStats = extractStats(hovered);
        Map<String, Double> equippedStats = extractStats(equipped);

        if (hoveredStats.isEmpty() && equippedStats.isEmpty()) return;

        // Build comparison lines
        boolean hasComparison = false;
        var tooltip = event.getToolTip();

        for (String attr : hoveredStats.keySet()) {
            double hovVal = hoveredStats.getOrDefault(attr, 0.0);
            double eqVal = equippedStats.getOrDefault(attr, 0.0);
            double diff = hovVal - eqVal;
            if (Math.abs(diff) < 0.01) continue;

            if (!hasComparison) {
                tooltip.add(Component.empty());
                tooltip.add(Component.literal("vs Equipped:").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                hasComparison = true;
            }

            String name = formatAttrName(attr);
            if (diff > 0) {
                tooltip.add(Component.literal("  \u25B2 +" + formatVal(diff) + " " + name).withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.literal("  \u25BC " + formatVal(diff) + " " + name).withStyle(ChatFormatting.RED));
            }
        }

        // Check attributes only on equipped (losses)
        for (String attr : equippedStats.keySet()) {
            if (hoveredStats.containsKey(attr)) continue;
            double eqVal = equippedStats.get(attr);
            if (Math.abs(eqVal) < 0.01) continue;

            if (!hasComparison) {
                tooltip.add(Component.empty());
                tooltip.add(Component.literal("vs Equipped:").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                hasComparison = true;
            }

            String name = formatAttrName(attr);
            tooltip.add(Component.literal("  \u25BC " + formatVal(-eqVal) + " " + name).withStyle(ChatFormatting.RED));
        }
    }

    private static ItemStack findEquippedComparison(ItemStack hovered) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return ItemStack.EMPTY;

        // Armor comparison — check Equippable component for armor slots
        Equippable equippable = hovered.get(DataComponents.EQUIPPABLE);
        if (equippable != null) {
            EquipmentSlot slot = equippable.slot();
            if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET) {
                return mc.player.getItemBySlot(slot);
            }
        }
        // Weapon/tool comparison — items with TOOL component or RPG weapons go against mainhand
        if (hovered.has(DataComponents.TOOL) || hovered.getItem().getClass().getName().contains("RpgWeaponItem")) {
            return mc.player.getMainHandItem();
        }
        // Items with attribute modifiers but no equipment slot — compare to mainhand
        ItemAttributeModifiers mods = hovered.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (mods != null && !mods.modifiers().isEmpty()) {
            return mc.player.getMainHandItem();
        }
        return ItemStack.EMPTY;
    }

    private static Map<String, Double> extractStats(ItemStack stack) {
        Map<String, Double> stats = new HashMap<>();
        ItemAttributeModifiers mods = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (var entry : mods.modifiers()) {
            Holder<Attribute> attr = entry.attribute();
            AttributeModifier mod = entry.modifier();
            String key = attr.getRegisteredName();
            double val = mod.amount();
            if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL ||
                mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
                val *= 100.0; // Show as percentage
            }
            stats.merge(key, val, Double::sum);
        }
        return stats;
    }

    private static String formatAttrName(String registryName) {
        // Extract last part after ':'
        int colon = registryName.lastIndexOf(':');
        String name = colon >= 0 ? registryName.substring(colon + 1) : registryName;
        // Convert snake_case to Title Case
        StringBuilder sb = new StringBuilder();
        for (String part : name.split("_")) {
            if (!sb.isEmpty()) sb.append(" ");
            if (!part.isEmpty()) sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    private static String formatVal(double val) {
        if (val == (int) val) return String.valueOf((int) val);
        return String.format("%.1f", val);
    }
}
