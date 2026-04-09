/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.npc.villager.Villager
 *  net.minecraft.world.entity.npc.villager.VillagerProfession
 *  net.minecraft.world.entity.npc.villager.VillagerTrades
 *  net.minecraft.world.entity.npc.villager.VillagerTrades$ItemListing
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.trading.MerchantOffer
 *  net.minecraft.world.item.trading.MerchantOffers
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 *  net.neoforged.neoforge.network.registration.PayloadRegistrar
 */
package com.ultra.megamod.feature.villagerrefresh;

import com.ultra.megamod.feature.villagerrefresh.RefreshTradesPayload;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class VillagerTradeRefresh {
    private static final double MAX_INTERACTION_DISTANCE = 6.0;
    public static void init(IEventBus modBus) {
        modBus.addListener(VillagerTradeRefresh::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");
        registrar.playToServer(RefreshTradesPayload.TYPE, RefreshTradesPayload.STREAM_CODEC, RefreshTradesPayload::handleOnServer);
        registrar.playToServer(AdminTradeEditPayload.TYPE, AdminTradeEditPayload.STREAM_CODEC, AdminTradeEditPayload::handleOnServer);
        registrar.playToClient(AdminTradeOffersPayload.TYPE, AdminTradeOffersPayload.STREAM_CODEC, AdminTradeOffersPayload::handleOnClient);
    }

    public static boolean isAllowed(String playerName) {
        return com.ultra.megamod.feature.computer.admin.AdminSystem.ADMIN_USERNAMES.contains(playerName);
    }

    public static void handleRefreshRequest(ServerPlayer player, int villagerEntityId) {
        if (!com.ultra.megamod.feature.computer.admin.AdminSystem.ADMIN_USERNAMES.contains(player.getGameProfile().name())) {
            return;
        }
        Entity entity = player.level().getEntity(villagerEntityId);
        if (!(entity instanceof Villager)) {
            return;
        }
        Villager villager = (Villager)entity;
        if ((double)player.distanceTo(villager) > 6.0) {
            return;
        }
        if (villager.getVillagerData().profession().is(VillagerProfession.NONE) || villager.getVillagerData().profession().is(VillagerProfession.NITWIT)) {
            return;
        }
        if (villager.getOffers().isEmpty()) {
            return;
        }
        boolean hasUsedTrades = false;
        for (MerchantOffer offer : villager.getOffers()) {
            if (offer.getUses() <= 0) continue;
            hasUsedTrades = true;
            break;
        }
        if (hasUsedTrades) {
            ItemStack heldItem = player.getMainHandItem();
            if (!heldItem.is(Items.GOLD_BLOCK)) {
                int goldSlot = player.getInventory().findSlotMatchingItem(new ItemStack(Items.GOLD_BLOCK));
                if (goldSlot == -1) {
                    player.sendSystemMessage(Component.literal("Trades are locked! Hold or carry a Gold Block to unlock.").withStyle(ChatFormatting.RED));
                    return;
                }
                player.getInventory().removeItem(goldSlot, 1);
            } else {
                heldItem.shrink(1);
            }
            for (MerchantOffer offer : villager.getOffers()) {
                offer.resetUses();
            }
            VillagerTradeRefresh.rerollTrades(villager);
            ServerLevel level = (ServerLevel) player.level();
            level.playSound(null, villager.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.NEUTRAL, 1.0f, 1.2f);
            level.sendParticles(ParticleTypes.COMPOSTER, villager.getX(), villager.getY() + 1.0, villager.getZ(), 20, 0.4, 0.6, 0.4, 0.0);
            player.sendSystemMessage(Component.literal("Trades unlocked and refreshed! (1 Gold Block consumed)").withStyle(ChatFormatting.GOLD));
        } else {
            VillagerTradeRefresh.rerollTrades(villager);
            ServerLevel level = (ServerLevel) player.level();
            level.playSound(null, villager.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0f, 1.0f);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, villager.getX(), villager.getY() + 1.0, villager.getZ(), 10, 0.3, 0.5, 0.3, 0.0);
            player.sendSystemMessage(Component.literal("Trades refreshed!").withStyle(ChatFormatting.GREEN));
        }
        player.closeContainer();
    }

    private static void rerollTrades(Villager villager) {
        ServerLevel serverLevel = (ServerLevel)villager.level();
        int currentLevel = villager.getVillagerData().level();
        ResourceKey<?> professionKey = villager.getVillagerData().profession().unwrapKey().orElse(null);
        if (professionKey == null) {
            return;
        }
        Int2ObjectMap<VillagerTrades.ItemListing[]> tradeMap = (Int2ObjectMap<VillagerTrades.ItemListing[]>)VillagerTrades.TRADES.get(professionKey);
        if (tradeMap == null || tradeMap.isEmpty()) {
            return;
        }
        MerchantOffers offers = villager.getOffers();
        offers.clear();
        for (int level = 1; level <= currentLevel; ++level) {
            VillagerTrades.ItemListing[] listings = (VillagerTrades.ItemListing[])tradeMap.get(level);
            if (listings == null) continue;
            VillagerTradeRefresh.addRandomOffers(serverLevel, villager, offers, listings, 2);
        }
    }

    private static void addRandomOffers(ServerLevel level, Villager villager, MerchantOffers offers, VillagerTrades.ItemListing[] listings, int slots) {
        ArrayList<VillagerTrades.ItemListing> available = new ArrayList<VillagerTrades.ItemListing>(Arrays.asList(listings));
        int added = 0;
        while (added < slots && !available.isEmpty()) {
            int index = villager.getRandom().nextInt(available.size());
            MerchantOffer offer = ((VillagerTrades.ItemListing)available.remove(index)).getOffer(level, villager, villager.getRandom());
            if (offer == null) continue;
            offers.add(offer);
            ++added;
        }
    }
}

