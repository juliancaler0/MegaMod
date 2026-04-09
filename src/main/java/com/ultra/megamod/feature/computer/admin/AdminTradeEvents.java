package com.ultra.megamod.feature.computer.admin;

import com.ultra.megamod.MegaMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;

/**
 * Admin-only villager trade overrides:
 * - No trade cooldown (uses reset after each trade so trades never lock)
 * - No price inflation (special price diff reset so prices stay base)
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class AdminTradeEvents {

    @SubscribeEvent
    public static void onTradeWithVillager(TradeWithVillagerEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) return;
        if (!AdminSystem.isAdmin(sp)) return;

        MerchantOffer offer = event.getMerchantOffer();

        // Reset uses so the trade never locks out
        offer.resetUses();

        // Reset price adjustments so prices don't inflate
        offer.setSpecialPriceDiff(0);

        // Also reset all other offers on this villager to prevent lockout
        AbstractVillager villager = event.getAbstractVillager();
        for (MerchantOffer other : villager.getOffers()) {
            other.resetUses();
            other.setSpecialPriceDiff(0);
        }
    }
}
