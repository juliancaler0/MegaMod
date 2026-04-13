package com.ultra.megamod.feature.worldedit.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * The WorldEdit selection wand. Left click a block = pos1, right click = pos2.
 * Actual selection logic is handled by {@link WorldEditToolEvents}.
 */
public class WandItem extends Item {
    public WandItem(Item.Properties props) {
        super(props.stacksTo(1).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal("WorldEdit Wand").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Left click: set position 1").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Right click: set position 2").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Admin only.").withStyle(ChatFormatting.RED));
    }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }
}
