package com.ultra.megamod.feature.dungeons.item;

import com.ultra.megamod.feature.relics.data.ArmorStatRoller;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class WroughtHelmItem extends Item {

    private static final double BASE_ARMOR = 3.0;
    private static final double BASE_TOUGHNESS = 1.0;

    public WroughtHelmItem(Item.Properties props) {
        super(props.stacksTo(1).durability(400).rarity(Rarity.RARE)
            .equippable(EquipmentSlot.HEAD));
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!ArmorStatRoller.isArmorInitialized(stack)) {
            ArmorStatRoller.rollAndApply(stack, BASE_ARMOR, BASE_TOUGHNESS, EquipmentSlot.HEAD, level.random);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        ArmorStatRoller.appendArmorTooltip(stack, tooltip);
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("A heavy wrought-iron helmet").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("High armor value").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
    }
}
