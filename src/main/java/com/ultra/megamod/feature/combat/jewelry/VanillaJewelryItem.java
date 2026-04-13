package com.ultra.megamod.feature.combat.jewelry;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Simple jewelry item without Accessories integration, ported 1:1 from the Jewelry mod reference.
 * Used as the default fallback when the Accessories system is not loaded.
 * Displays optional lore text in the tooltip.
 */
public class VanillaJewelryItem extends Item implements JewelryMarkerItem {
    private final String lore;

    public VanillaJewelryItem(Properties settings, String lore) {
        super(settings);
        this.lore = lore;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                 Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        if (lore != null && !lore.isEmpty()) {
            tooltip.accept(Component.translatable(lore).withStyle(ChatFormatting.ITALIC, ChatFormatting.GOLD));
        }
    }
}
