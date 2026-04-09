package com.ultra.megamod.feature.dungeons.item;

import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class LifeStealerItem extends Item {

    private static final float BASE_DAMAGE = 7.0f;
    private static final float LIFESTEAL_PERCENT = 0.2f;

    public LifeStealerItem(Item.Properties props) {
        super(props.stacksTo(1).durability(700).rarity(Rarity.EPIC));
    }

    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!WeaponStatRoller.isWeaponInitialized(stack)) {
            WeaponStatRoller.rollAndApply(stack, BASE_DAMAGE, level.random);
        }
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (!attacker.level().isClientSide()) {
            float healAmount = 7.0f * LIFESTEAL_PERCENT;
            attacker.heal(healAmount);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        WeaponStatRoller.appendWeaponTooltip(stack, tooltip);
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("A cursed blade that drinks blood").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Heals 20% of damage dealt on hit").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
    }
}
