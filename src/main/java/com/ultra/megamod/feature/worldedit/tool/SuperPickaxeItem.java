package com.ultra.megamod.feature.worldedit.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/** SuperPickaxe — instantly breaks blocks on left-click. Vein/area variants
 *  are selected via commands. */
public class SuperPickaxeItem extends Item {
    public SuperPickaxeItem(Item.Properties props) {
        super(props.stacksTo(1).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal("Super Pickaxe").withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Left click: break targeted block").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Admin only.").withStyle(ChatFormatting.RED));
    }
}
