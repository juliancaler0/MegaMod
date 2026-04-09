package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.animation.WeaponAttributeRegistry;
import com.ultra.megamod.feature.combat.animation.WeaponAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Enhanced weapon tooltips showing attack range, combo info, and dual-wield support.
 * Ported from BetterCombat (net.bettercombat.client.WeaponAttributeTooltip).
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class WeaponAttributeTooltip {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        WeaponAttributes attrs = WeaponAttributeRegistry.getAttributes(stack);
        if (attrs == null || attrs.attacks() == null || attrs.attacks().length == 0) return;

        var tooltip = event.getToolTip();

        // Add attack range info
        double range = 3.0 + attrs.rangeBonus();
        if (attrs.attackRange() > 0) range = attrs.attackRange();
        tooltip.add(Component.literal(" ")
                .append(Component.translatable("tooltip.megamod.attack_range",
                        String.format("%.1f", range)))
                .withStyle(ChatFormatting.DARK_GREEN));

        // Add combo info
        if (attrs.attacks().length > 1) {
            tooltip.add(Component.literal(" ")
                    .append(Component.translatable("tooltip.megamod.combo_attacks",
                            attrs.attacks().length))
                    .withStyle(ChatFormatting.GOLD));
        }

        // Add two-handed indicator
        if (attrs.twoHanded()) {
            tooltip.add(Component.literal(" ")
                    .append(Component.translatable("tooltip.megamod.two_handed"))
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
