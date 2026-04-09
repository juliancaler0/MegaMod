/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Rarity
 */
package com.ultra.megamod.feature.dungeons.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class DungeonMiniKeyItem
extends Item {
    public DungeonMiniKeyItem(Item.Properties props) {
        super(props.stacksTo(16).rarity(Rarity.UNCOMMON));
    }
}

