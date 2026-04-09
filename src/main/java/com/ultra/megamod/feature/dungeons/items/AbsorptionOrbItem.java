package com.ultra.megamod.feature.dungeons.items;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

public class AbsorptionOrbItem extends Item {

    public AbsorptionOrbItem(Item.Properties props) {
        super(props);
    }

    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!(entity instanceof Player player)) {
            return;
        }
        if (player.getOffhandItem() != stack) {
            return;
        }
        if (level.getGameTime() % 40L != 0L) {
            return;
        }
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty() || !mainHand.isDamaged()) {
            return;
        }
        mainHand.setDamageValue(mainHand.getDamageValue() - 1);
        stack.hurtAndBreak(1, (ServerPlayer) player, EquipmentSlot.OFFHAND);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal("An orb that absorbs damage from your tools").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Hold in offhand to repair mainhand item").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("250 uses").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
    }
}
