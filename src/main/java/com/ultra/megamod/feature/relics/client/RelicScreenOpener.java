package com.ultra.megamod.feature.relics.client;

import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.network.OpenRelicScreenPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Client-only helper to open the relic description screen from a deferred payload.
 */
public class RelicScreenOpener {
    public static void openFromPayload(OpenRelicScreenPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        String relicName = payload.relicName();

        // Find the relic in the player's inventory
        ItemStack relicStack = ItemStack.EMPTY;
        RelicItem relicItem = null;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof RelicItem ri
                    && stack.getHoverName().getString().equalsIgnoreCase(relicName)) {
                relicStack = stack;
                relicItem = ri;
                break;
            }
        }

        if (relicItem != null) {
            mc.setScreen(new RelicDescriptionScreen(relicStack, relicItem));
        }
    }
}
