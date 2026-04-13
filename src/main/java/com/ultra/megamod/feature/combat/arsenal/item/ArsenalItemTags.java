package com.ultra.megamod.feature.combat.arsenal.item;

import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.feature.combat.arsenal.ArsenalMod;

public class ArsenalItemTags {
    public static final TagKey<Item> ALL = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ArsenalMod.NAMESPACE, "arsenal_all"));
}
