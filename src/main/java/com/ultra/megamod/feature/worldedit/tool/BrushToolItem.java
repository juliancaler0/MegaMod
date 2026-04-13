package com.ultra.megamod.feature.worldedit.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/** A brush tool item — when held and right-clicked, fires the player's bound brush. */
public class BrushToolItem extends Item {
    public BrushToolItem(Item.Properties props) {
        super(props.stacksTo(1).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal("WorldEdit Brush").withStyle(ChatFormatting.AQUA));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Right click to apply bound brush.").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Bind with /we_brush sphere 1 stone").withStyle(ChatFormatting.GRAY));
    }
}
