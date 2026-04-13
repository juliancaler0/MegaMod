package com.ultra.megamod.feature.combat.archers.client.util;

import com.ultra.megamod.feature.combat.archers.item.misc.AutoFireHook;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Adds tooltip line to items that have the AutoFireHook applied.
 * Uses NeoForge's ItemTooltipEvent instead of Fabric's ItemTooltipCallback.
 */
@EventBusSubscriber(modid = "megamod", value = net.neoforged.api.distmarker.Dist.CLIENT)
public class ArchersTooltip {
    public static void init() {
        // Registration handled by @EventBusSubscriber annotation
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (AutoFireHook.isApplied(event.getItemStack())) {
            event.getToolTip().add(1, Component.translatable(AutoFireHook.item().getDescriptionId())
                    .withStyle(ChatFormatting.DARK_GREEN));
        }
    }
}
