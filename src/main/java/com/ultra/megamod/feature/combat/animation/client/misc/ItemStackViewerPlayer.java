package com.ultra.megamod.feature.combat.animation.client.misc;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for player entities to track which ItemStack is being viewed for tooltip rendering.
 * Ported 1:1 from BetterCombat (net.bettercombat.client.misc.ItemStackViewerPlayer).
 */
public interface ItemStackViewerPlayer {
    void betterCombat_setViewedItemStack(@Nullable ItemStack itemStack);
    ItemStack betterCombat_getViewedItemStack();
}
