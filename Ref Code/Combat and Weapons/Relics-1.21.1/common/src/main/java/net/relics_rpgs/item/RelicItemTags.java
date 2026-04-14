package net.relics_rpgs.item;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.relics_rpgs.RelicsMod;

public class RelicItemTags {
    public static final TagKey<Item> ALL = TagKey.of(RegistryKeys.ITEM, Identifier.of(RelicsMod.NAMESPACE, "all"));
}
