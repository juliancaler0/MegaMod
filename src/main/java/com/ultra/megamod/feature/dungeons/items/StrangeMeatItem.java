package com.ultra.megamod.feature.dungeons.items;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class StrangeMeatItem extends Item {

    public StrangeMeatItem(Item.Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        stack.shrink(1);
        if (!level.isClientSide() && entity instanceof Player player) {
            int roll = level.getRandom().nextInt(5);
            switch (roll) {
                case 0 -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
                case 1 -> player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
                case 2 -> player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 300, 0));
                case 3 -> player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 0));
                case 4 -> player.addEffect(new MobEffectInstance(MobEffects.HASTE, 200, 1));
            }
            player.getFoodData().eat(4, 0.6f);
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal("Mysterious meat from the dungeon depths").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Grants a random effect when eaten").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
    }
}
