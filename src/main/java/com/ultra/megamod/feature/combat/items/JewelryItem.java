package com.ultra.megamod.feature.combat.items;

import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import java.util.Collections;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * A simple RelicItem subclass for jewelry (rings and necklaces).
 * Has no relic abilities but passes {@code instanceof RelicItem} checks
 * so it can be equipped in accessory slots.
 */
public class JewelryItem extends RelicItem {

    public JewelryItem(Item.Properties props, AccessorySlotType slotType) {
        super("Jewelry", slotType, Collections.emptyList(), props);
    }

    /**
     * Override tooltip to show only the slot type and attribute modifiers
     * (which are handled by the Item.Properties attributes).
     * Skips the relic-specific tooltip lines (level, quality, XP, abilities)
     * since jewelry has no relic data.
     */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Slot: " + this.getSlotType().getDisplayName()).withStyle(ChatFormatting.GRAY));
    }
}
