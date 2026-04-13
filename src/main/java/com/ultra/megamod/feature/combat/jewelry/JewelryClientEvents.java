package com.ultra.megamod.feature.combat.jewelry;

import com.ultra.megamod.MegaMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Client-side event handler for jewelry items, ported 1:1 from the Jewelry mod reference.
 * Removes duplicate tooltip lines that can appear when jewelry has multiple ring slots
 * (e.g., Ring Left and Ring Right both showing the same attribute).
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class JewelryClientEvents {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof JewelryMarkerItem
                || stack.getItem() instanceof JewelryAccessoriesItem) {
            removeTooltipDuplicates(event);
        }
    }

    /**
     * Removes duplicate lines from a jewelry item's tooltip.
     * Necessary for installations where multiple Ring slots exist and
     * each slot contributes identical attribute modifier lines.
     */
    private static void removeTooltipDuplicates(ItemTooltipEvent event) {
        var tooltip = event.getToolTip();
        for (int i = 0; i < tooltip.size(); i++) {
            Component text = tooltip.get(i);
            for (int j = i + 1; j < tooltip.size(); j++) {
                if (text.getString().equals(tooltip.get(j).getString())) {
                    tooltip.remove(j);
                    j--;
                }
            }
        }
    }
}
