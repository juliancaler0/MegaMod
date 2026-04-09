package com.ultra.megamod.feature.dungeons.items;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class LivingDiviningRodItem extends Item {

    public LivingDiviningRodItem(Item.Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        AABB area = player.getBoundingBox().inflate(24.0);
        List<Monster> monsters = serverLevel.getEntitiesOfClass(Monster.class, area);

        for (Monster monster : monsters) {
            monster.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
        }

        ItemStack stack = player.getItemInHand(hand);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        player.getCooldowns().addCooldown(stack, 120);

        level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.5f);

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal("A rod pulsing with otherworldly energy").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Right-click to reveal nearby enemies").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("100 uses | 6s cooldown").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
    }
}
