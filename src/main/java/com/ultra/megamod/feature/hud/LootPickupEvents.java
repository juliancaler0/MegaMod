package com.ultra.megamod.feature.hud;

import com.ultra.megamod.feature.computer.network.handlers.SettingsHandler;
import com.ultra.megamod.feature.hud.network.LootPickupPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Server-side: sends loot pickup notifications to the client.
 */
@EventBusSubscriber(modid = "megamod")
public class LootPickupEvents {

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer sp)) return;
        if (!SettingsHandler.isEnabled(sp.getUUID(), "hud_loot_log")) return;

        ItemStack stack = event.getOriginalStack();
        if (stack.isEmpty()) return;

        String name = stack.getHoverName().getString();
        int count = stack.getCount();
        PacketDistributor.sendToPlayer(sp, new LootPickupPayload(name, count));
    }
}
