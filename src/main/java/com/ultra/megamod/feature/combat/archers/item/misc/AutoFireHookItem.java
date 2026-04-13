package com.ultra.megamod.feature.combat.archers.item.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class AutoFireHookItem extends Item {
    public AutoFireHookItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        tooltip.accept(Component.translatable(this.getDescriptionId() + ".description_1").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable(this.getDescriptionId() + ".description_2").withStyle(ChatFormatting.GRAY));
    }
}
