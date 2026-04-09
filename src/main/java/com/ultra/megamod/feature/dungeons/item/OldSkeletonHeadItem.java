package com.ultra.megamod.feature.dungeons.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class OldSkeletonHeadItem extends Item {
    public OldSkeletonHeadItem(Item.Properties props) {
        super(props.stacksTo(16).rarity(Rarity.RARE)
                .equippable(EquipmentSlot.HEAD));
    }
}
