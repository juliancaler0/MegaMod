/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.component.TooltipDisplay
 */
package com.ultra.megamod.feature.dungeons.items;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class SoulAnchorItem
extends Item {
    public SoulAnchorItem(Item.Properties props) {
        super(props.stacksTo(1));
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept((Component)Component.literal((String)"Dungeon Insurance").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.accept((Component)Component.empty());
        tooltip.accept((Component)Component.literal((String)"If you die in a dungeon while").withStyle(ChatFormatting.GRAY));
        tooltip.accept((Component)Component.literal((String)"carrying this, your gear is saved.").withStyle(ChatFormatting.GRAY));
        tooltip.accept((Component)Component.empty());
        tooltip.accept((Component)Component.literal((String)"Consumed on use.").withStyle(ChatFormatting.RED));
    }

    public boolean isFoil(ItemStack stack) {
        return true;
    }
}

