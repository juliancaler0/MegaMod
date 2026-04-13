package com.ultra.megamod.feature.combat.archers.item;

import com.ultra.megamod.feature.combat.archers.ArchersMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public class Group {
    public static Identifier ID = Identifier.fromNamespaceAndPath(ArchersMod.ID, "archers");
    public static ResourceKey<CreativeModeTab> KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ID);
    public static CreativeModeTab ARCHERS;
}
