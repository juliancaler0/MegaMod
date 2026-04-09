package com.ultra.megamod.feature.relics.accessory;

import com.ultra.megamod.feature.relics.accessory.AccessoryManager;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.feature.relics.network.AccessoryPayload;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid="megamod")
public class AccessoryEvents {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        if (overworld.getGameTime() % 1200L != 0L) {
            return;
        }
        AccessoryManager.get(overworld).saveToDisk(overworld);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        AccessoryManager.get(overworld).saveToDisk(overworld);
        AccessoryManager.reset();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        net.minecraft.server.MinecraftServer server = player2.level().getServer();
        if (server == null) return;
        ServerLevel overworld = server.overworld();
        AccessoryManager manager = AccessoryManager.get(overworld);
        manager.getAllEquipped(player2.getUUID());
        AccessoryEvents.syncToClient(player2);
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        if (player.level().getGameRules().get(GameRules.KEEP_INVENTORY)) {
            return;
        }
        net.minecraft.server.MinecraftServer server = player.level().getServer();
        if (server == null) return;
        ServerLevel overworld = server.overworld();
        AccessoryManager manager = AccessoryManager.get(overworld);
        UUID playerId = player.getUUID();
        Map<AccessorySlotType, ItemStack> equipped = manager.getAllEquipped(playerId);
        for (Map.Entry<AccessorySlotType, ItemStack> entry : equipped.entrySet()) {
            ItemStack stack = entry.getValue();
            if (!stack.isEmpty()) {
                player.spawnAtLocation((ServerLevel) player.level(), stack.copy());
            }
            manager.removeEquipped(playerId, entry.getKey());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        net.minecraft.server.MinecraftServer server = player2.level().getServer();
        if (server == null) return;
        ServerLevel overworld = server.overworld();
        AccessoryManager.get(overworld).saveToDisk(overworld);
    }

    public static void syncToClient(ServerPlayer player) {
        net.minecraft.server.MinecraftServer server = player.level().getServer();
        if (server == null) return;
        ServerLevel overworld = server.overworld();
        AccessoryManager manager = AccessoryManager.get(overworld);
        Map<AccessorySlotType, ItemStack> equipped = manager.getAllEquipped(player.getUUID());
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<AccessorySlotType, ItemStack> entry : equipped.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            String itemId = BuiltInRegistries.ITEM.getKey(entry.getValue().getItem()).toString();
            tag.putString(entry.getKey().name(), itemId);
        }
        AccessoryPayload.AccessorySyncPayload payload = new AccessoryPayload.AccessorySyncPayload(tag);
        PacketDistributor.sendToPlayer(player, payload);
    }
}

