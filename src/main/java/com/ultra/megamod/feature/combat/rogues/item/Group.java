package com.ultra.megamod.feature.combat.rogues.item;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Creative tab key references for Rogues & Warriors items.
 * Ported from net.rogues.item.Group.
 *
 * Items are added to the existing MegaMod creative tabs via
 * {@link com.ultra.megamod.feature.combat.CombatCreativeTab}.
 */
public class Group {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MegaMod.MODID, "megamod_weapons_tab");
    public static final ResourceKey<CreativeModeTab> KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ID);
}
