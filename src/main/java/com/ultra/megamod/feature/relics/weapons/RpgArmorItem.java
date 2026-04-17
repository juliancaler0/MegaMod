package com.ultra.megamod.feature.relics.weapons;

import com.ultra.megamod.feature.combat.items.EquipmentSetManager;
import com.ultra.megamod.feature.relics.data.ArmorStatRoller;
import com.ultra.megamod.feature.relics.data.WeaponRarity;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
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

    // Stat rolling disabled — source-parity attributes only (rolls come later).
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        // no-op
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        appendSetBonusTooltip(stack, tooltip);
    }

    private void appendSetBonusTooltip(ItemStack stack, Consumer<Component> tooltip) {
        String stackId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        for (EquipmentSetManager.EquipmentSet set : EquipmentSetManager.getAllSets()) {
            if (!set.itemIds().contains(stackId)) continue;
            tooltip.accept(Component.empty());
            tooltip.accept(Component.literal("Set: " + set.displayName())
                .withStyle(ChatFormatting.GOLD));
            for (EquipmentSetManager.SetBonus bonus : set.bonuses()) {
                tooltip.accept(Component.literal("  (" + bonus.requiredPieces() + ") Set Bonus:")
                    .withStyle(ChatFormatting.DARK_GRAY));
                for (var entry : bonus.attributeModifiers().entrySet()) {
                    String attrName = attributeDisplayName(entry.getKey());
                    double value = entry.getValue();
                    String sign = value >= 0 ? "+" : "";
                    tooltip.accept(Component.literal("    " + sign + formatAttributeValue(value) + " " + attrName)
                        .withStyle(ChatFormatting.DARK_GREEN));
                }
            }
            return;
        }
    }

    private static String attributeDisplayName(Holder<Attribute> holder) {
        return holder.unwrapKey()
            .map(key -> {
                Identifier id = key.identifier();
                String path = id.getPath();
                int dot = path.lastIndexOf('.');
                String name = dot >= 0 ? path.substring(dot + 1) : path;
                StringBuilder sb = new StringBuilder(name.length());
                boolean cap = true;
                for (char c : name.toCharArray()) {
                    if (c == '_') { sb.append(' '); cap = true; }
                    else if (cap) { sb.append(Character.toUpperCase(c)); cap = false; }
                    else sb.append(c);
                }
                return sb.toString();
            })
            .orElse("Unknown");
    }

    private static String formatAttributeValue(double value) {
        if (value == Math.floor(value)) return String.format("%.0f", value);
        return String.format("%.1f", value);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    public double getBaseArmor() { return baseArmor; }
    public double getBaseToughness() { return baseToughness; }
    public EquipmentSlot getArmorSlot() { return armorSlot; }
}
