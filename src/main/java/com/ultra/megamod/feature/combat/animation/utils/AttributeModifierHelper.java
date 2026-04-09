package com.ultra.megamod.feature.combat.animation.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import javax.annotation.Nonnull;

/**
 * Extracts attribute modifiers from ItemStacks for dual-wield attribute swapping.
 * Ported 1:1 from BetterCombat (net.bettercombat.utils.AttributeModifierHelper).
 */
public class AttributeModifierHelper {

    @Nonnull
    public static Multimap<Holder<Attribute>, AttributeModifier> modifierMultimap(ItemStack itemStack) {
        var modifiers = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        Multimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();
        for (var entry : modifiers.modifiers()) {
            map.put(entry.attribute(), entry.modifier());
        }
        return map;
    }

    @Nonnull
    public static Multimap<Holder<Attribute>, AttributeModifier> fromModifier(
            Holder<Attribute> attribute, AttributeModifier modifier) {
        Multimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();
        map.put(attribute, modifier);
        return map;
    }
}
