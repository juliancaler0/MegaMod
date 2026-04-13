package com.ultra.megamod.feature.combat.rogues.block;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.items.WorkbenchBlock;
import net.minecraft.resources.Identifier;

/**
 * Martial Workbench (Arms Workbench) block reference.
 * Ported from net.rogues.block.MartialWorkbenchBlock.
 *
 * The actual block implementation is {@link WorkbenchBlock} registered in
 * {@link com.ultra.megamod.feature.combat.runes.RuneWorkbenchRegistry}.
 * This class provides the ID constant for reference.
 */
public class MartialWorkbenchBlock {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MegaMod.MODID, "arms_workbench");
}
