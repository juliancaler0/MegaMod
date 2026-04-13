package com.ultra.megamod.feature.worldedit.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/** Long-range wand: raycasts up to 300 blocks to set the selection corners. */
public class FarWandItem extends Item {
    public FarWandItem(Item.Properties props) {
        super(props.stacksTo(1).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal("Far Wand").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.accept(Component.literal("Left click in air: set pos1 at distance").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Right click in air: set pos2 at distance").withStyle(ChatFormatting.GRAY));
    }
}
