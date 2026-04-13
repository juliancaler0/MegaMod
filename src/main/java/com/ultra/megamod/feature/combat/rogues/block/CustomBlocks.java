package com.ultra.megamod.feature.combat.rogues.block;

import com.ultra.megamod.feature.combat.runes.RuneWorkbenchRegistry;
import net.minecraft.world.level.block.Block;

/**
 * Block references for Rogues & Warriors content.
 * Ported from net.rogues.block.CustomBlocks.
 *
 * The Arms Workbench block is registered through {@link RuneWorkbenchRegistry#ARMS_WORKBENCH}.
 * This class provides convenience access.
 */
public class CustomBlocks {

    /**
     * Get the Arms Workbench block.
     * Used as the POI workstation for the Arms Merchant villager profession.
     */
    public static Block getArmsWorkbench() {
        return RuneWorkbenchRegistry.ARMS_WORKBENCH.get();
    }
}
