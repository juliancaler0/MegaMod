package com.ultra.megamod.feature.dungeons.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class OldSkeletonBoneItem extends Item {
    public OldSkeletonBoneItem(Item.Properties props) {
        super(props.stacksTo(64).rarity(Rarity.COMMON));
    }
}
