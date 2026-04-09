package com.ultra.megamod.feature.dungeons.item;

import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
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

public class WroughtAxeItem extends Item {

    private static final float BASE_DAMAGE = 11.0f;

    public WroughtAxeItem(Item.Properties props) {
        super(props.stacksTo(1).durability(800).rarity(Rarity.RARE));
    }

    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!WeaponStatRoller.isWeaponInitialized(stack)) {
            WeaponStatRoller.rollAndApply(stack, BASE_DAMAGE, level.random);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        WeaponStatRoller.appendWeaponTooltip(stack, tooltip);
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("A brutal wrought-iron axe").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Very high damage, very slow").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
    }
}
