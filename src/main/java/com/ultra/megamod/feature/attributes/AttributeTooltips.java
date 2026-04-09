/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemAttributeModifiers
 *  net.minecraft.world.item.component.ItemAttributeModifiers$Entry
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.ItemTooltipEvent
 */
package com.ultra.megamod.feature.attributes;

import com.ultra.megamod.feature.attributes.MegaModAttributes;
import com.ultra.megamod.feature.relics.data.ArmorStatRoller;
import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid="megamod", value={Dist.CLIENT})
public class AttributeTooltips {
    private static final Map<Holder<Attribute>, String> ATTRIBUTE_DISPLAY_NAMES = new LinkedHashMap<Holder<Attribute>, String>();

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        // Skip items that already show their own comprehensive stat tooltips
        if (WeaponStatRoller.isWeaponInitialized(stack) || ArmorStatRoller.isArmorInitialized(stack)) {
            return;
        }
        ItemAttributeModifiers modifiers = (ItemAttributeModifiers)stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, (Object)ItemAttributeModifiers.EMPTY);
        if (modifiers == ItemAttributeModifiers.EMPTY) {
            return;
        }
        List tooltip = event.getToolTip();
        boolean headerAdded = false;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            Object formattedValue;
            Holder attributeHolder = entry.attribute();
            AttributeModifier modifier = entry.modifier();
            String displayName = ATTRIBUTE_DISPLAY_NAMES.get(attributeHolder);
            if (displayName == null) continue;
            if (!headerAdded) {
                tooltip.add(Component.empty());
                tooltip.add(Component.literal((String)"MegaMod Bonuses:").withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD}));
                headerAdded = true;
            }
            double amount = modifier.amount();
            AttributeModifier.Operation operation = modifier.operation();
            if (operation == AttributeModifier.Operation.ADD_VALUE) {
                formattedValue = amount == (double)((long)amount) ? String.format("%+.0f", amount) : String.format("%+.1f", amount);
                formattedValue = (String)formattedValue + " " + displayName;
            } else {
                double displayAmount = amount * 100.0;
                if (operation == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    displayAmount = amount * 100.0;
                }
                formattedValue = displayAmount == (double)((long)displayAmount) ? String.format("%+.0f%%", displayAmount) : String.format("%+.1f%%", displayAmount);
                formattedValue = (String)formattedValue + " " + displayName;
            }
            // Source identification from modifier ID
            String modPath = modifier.id().getPath();
            String source = "";
            if (modPath.startsWith("weapon_bonus_") || modPath.equals("weapon_base_damage")) source = " (Weapon)";
            else if (modPath.startsWith("skill_")) source = " (Skill)";
            else if (modPath.contains("relic")) source = " (Relic)";

            ChatFormatting color = amount >= 0.0 ? ChatFormatting.GOLD : ChatFormatting.RED;
            String prefix = amount >= 0.0 ? " " : " ";
            String line = prefix + (String)formattedValue;
            if (!source.isEmpty()) {
                tooltip.add(Component.literal(line).withStyle(color)
                    .append(Component.literal(source).withStyle(ChatFormatting.DARK_GRAY)));
            } else {
                tooltip.add(Component.literal(line).withStyle(color));
            }
        }
    }

    static {
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.FIRE_DAMAGE_BONUS, "Fire Damage");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.ICE_DAMAGE_BONUS, "Ice Damage");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.LIGHTNING_DAMAGE_BONUS, "Lightning Damage");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.POISON_DAMAGE_BONUS, "Poison Damage");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.HOLY_DAMAGE_BONUS, "Holy Damage");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.SHADOW_DAMAGE_BONUS, "Shadow Damage");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.CRITICAL_DAMAGE, "Critical Damage");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.CRITICAL_CHANCE, "Critical Chance");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.FIRE_RESISTANCE_BONUS, "Fire Resistance");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.ICE_RESISTANCE_BONUS, "Ice Resistance");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.LIGHTNING_RESISTANCE_BONUS, "Lightning Resistance");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.POISON_RESISTANCE_BONUS, "Poison Resistance");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.HOLY_RESISTANCE_BONUS, "Holy Resistance");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.SHADOW_RESISTANCE_BONUS, "Shadow Resistance");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.MINING_SPEED_BONUS, "Mining Speed");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.SWIM_SPEED_BONUS, "Swim Speed");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.DODGE_CHANCE, "Dodge Chance");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.JUMP_HEIGHT_BONUS, "Jump Height");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.FALL_DAMAGE_REDUCTION, "Fall Damage Reduction");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.HEALTH_REGEN_BONUS, "Health Regen");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.HUNGER_EFFICIENCY, "Hunger Efficiency");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.XP_BONUS, "XP Bonus");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.LOOT_FORTUNE, "Loot Fortune");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.MEGACOIN_BONUS, "MegaCoin Bonus");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.SHOP_DISCOUNT, "Shop Discount");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.SELL_BONUS, "Sell Bonus");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.LIFESTEAL, "Lifesteal");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.THORNS_DAMAGE, "Thorns Damage");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.ARMOR_SHRED, "Armor Shred");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.COMBO_SPEED, "Combo Speed");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.STUN_CHANCE, "Stun Chance");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.COOLDOWN_REDUCTION, "Cooldown Reduction");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.ABILITY_POWER, "Ability Power");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.MANA_EFFICIENCY, "Mana Efficiency");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.SPELL_RANGE, "Spell Range");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.COMBAT_XP_BONUS, "Combat XP");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.MINING_XP_BONUS, "Mining XP");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.FARMING_XP_BONUS, "Farming XP");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.ARCANE_XP_BONUS, "Arcane XP");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.SURVIVAL_XP_BONUS, "Survival XP");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.BRILLIANCE, "Brilliance");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.BEAST_AFFINITY, "Beast Affinity");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.PREY_SENSE, "Prey Sense");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.VEIN_SENSE, "Vein Sense");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.EXCAVATION_REACH, "Excavation Reach");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.ARCANE_POWER, "Arcane Power");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.HEALING_POWER, "Healing Power");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.SOUL_POWER, "Soul Power");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.SPELL_HASTE, "Spell Haste");
        ATTRIBUTE_DISPLAY_NAMES.put((Holder<Attribute>)MegaModAttributes.RANGED_DAMAGE, "Ranged Damage");
    }
}

