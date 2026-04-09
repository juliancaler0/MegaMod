package com.ultra.megamod.feature.dungeons.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class RatFangItem extends Item {
    public RatFangItem(Item.Properties props) {
        super(props.stacksTo(16).rarity(Rarity.UNCOMMON));
    }
}
