package com.ultra.megamod.mixin.spellengine.client;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * In 1.21.11, appendAttributeModifierTooltip was removed from ItemStack.
 * Attribute tooltip formatting is now done via ItemAttributeModifiers.Display.apply().
 * This class provides a static helper that replicates the old behavior using the new API.
 */
public class ItemStackTooltipAccessor {

    /**
     * Formats an attribute modifier tooltip line using the standard display format,
     * replicating the behavior of the removed appendAttributeModifierTooltip.
     */
    public static void appendAttributeModifierTooltip(
            Consumer<Component> textConsumer,
            @Nullable Player player,
            Holder<Attribute> attribute,
            AttributeModifier modifier) {
        ItemAttributeModifiers.Display.attributeModifiers().apply(textConsumer, player, attribute, modifier);
    }
}
