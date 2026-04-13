package com.ultra.megamod.feature.worldedit.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/** Info tool — left click a block to display its id/state/light/biome. */
public class InfoToolItem extends Item {
    public InfoToolItem(Item.Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal("Info Tool").withStyle(ChatFormatting.YELLOW));
        tooltip.accept(Component.literal("Left click a block to inspect it").withStyle(ChatFormatting.GRAY));
    }
}
