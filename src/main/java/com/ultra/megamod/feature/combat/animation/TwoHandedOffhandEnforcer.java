package com.ultra.megamod.feature.combat.animation;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.animation.WeaponAttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Server-side enforcer that prevents the offhand slot from holding anything while a
 * two-handed weapon is in the main hand. If the player somehow equips a 2H weapon
 * with an item already in offhand, the offhand item is moved back into the main
 * inventory (or dropped if inventory is full) and the player is notified.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class TwoHandedOffhandEnforcer {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // Check every 5 ticks is fine — players won't notice 1/4s lag
        if (player.tickCount % 5 != 0) return;

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        if (off.isEmpty() || main.isEmpty()) return;

        var mainAttrs = WeaponAttributeRegistry.getAttributes(main);
        if (mainAttrs == null || !mainAttrs.twoHanded()) return;

        // Move offhand item to inventory; drop if inventory is full
        ItemStack toMove = off.copy();
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        boolean added = player.getInventory().add(toMove);
        if (!added) {
            player.drop(toMove, false);
        }
        player.displayClientMessage(
                Component.literal("Cannot hold offhand item with a two-handed weapon.")
                        .withStyle(ChatFormatting.YELLOW), true);
    }
}
