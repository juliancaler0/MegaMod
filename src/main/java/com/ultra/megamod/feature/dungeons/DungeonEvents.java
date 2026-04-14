/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingDeathEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.neoforged.neoforge.event.server.ServerStoppingEvent
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.ultra.megamod.feature.dungeons;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.dimensions.DimensionHelper;
import com.ultra.megamod.feature.dimensions.PocketManager;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.dungeons.DungeonManager;
import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
import com.ultra.megamod.feature.dungeons.insurance.InsuranceManager;
import com.ultra.megamod.feature.dungeons.items.SoulAnchorItem;
import com.ultra.megamod.feature.dungeons.network.DungeonSyncPayload;
import com.ultra.megamod.feature.relics.accessory.LibAccessoryLookup;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid="megamod")
public class DungeonEvents {
    private static final int AUTO_SAVE_INTERVAL = 1200;

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onPlayerDeathInDungeon(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        ServerLevel overworld = player.level().getServer().overworld();
        DungeonManager manager = DungeonManager.get(overworld);
        DungeonManager.DungeonInstance instance = manager.getDungeonForPlayer(player.getUUID());
        if (instance == null) {
            return;
        }
        boolean hasSoulAnchor = false;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof SoulAnchorItem)) continue;
            stack.shrink(1);
            hasSoulAnchor = true;
            break;
        }
        if (hasSoulAnchor) {
            // Soul Anchor: cancel death, half HP, teleport out — dungeon stays for party
            event.setCanceled(true);
            player.setHealth(player.getMaxHealth() / 2.0f);
            player.removeAllEffects();
            player.sendSystemMessage((Component)Component.literal((String)"Your Soul Anchor shattered, preserving your gear!").withStyle(ChatFormatting.LIGHT_PURPLE));
            // Record analytics for admin Dungeon Analytics tab (soul anchor extract)
            com.ultra.megamod.feature.computer.network.handlers.DungeonAnalyticsHandler.recordRun(
                    player.getGameProfile().name(), player.getUUID().toString(),
                    instance.tier.getDisplayName(), instance.theme.getDisplayName(),
                    "Extracted", instance.startTimeMs, System.currentTimeMillis(), "");
            manager.removePlayerFromDungeonOnDeath(player);
            removePlayerFromAllBossBars(player);
            PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new DungeonSyncPayload("", "", "", 0, 0, false), (CustomPacketPayload[])new CustomPacketPayload[0]);
            DimensionHelper.teleportBack(player);
            // Check if this was the last player — if so, cleanup
            if (!manager.hasRemainingPlayersInDungeon(instance.instanceId, overworld)) {
                manager.cleanupDungeon(instance.instanceId, overworld);
            }
            return;
        }
        // No Soul Anchor: check insurance, then lose uninsured items
        Set<String> insuredSlots = InsuranceManager.getInsuredSlots(instance.instanceId, player.getUUID());
        Map<String, ItemStack> savedItems = new HashMap<>();
        if (insuredSlots != null && !insuredSlots.isEmpty()) {
            // Save insured items before clearing
            for (String slot : insuredSlots) {
                ItemStack item = InsuranceManager.getItemForSlot(player, slot, overworld);
                if (!item.isEmpty()) {
                    savedItems.put(slot, item.copy());
                }
            }
        }
        inventory.clearContent();
        // Restore insured items to their slots
        if (!savedItems.isEmpty()) {
            for (Map.Entry<String, ItemStack> entry : savedItems.entrySet()) {
                restoreItemToSlot(player, entry.getKey(), entry.getValue(), overworld);
            }
            player.sendSystemMessage(Component.literal("Insurance saved " + savedItems.size() + " item(s)!").withStyle(ChatFormatting.GREEN));
        }
        if (savedItems.size() < (insuredSlots != null ? insuredSlots.size() : 0) || savedItems.isEmpty()) {
            player.sendSystemMessage((Component)Component.literal((String)"You perished in the dungeon! Uninsured items lost.").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD}));
        } else if (insuredSlots == null || insuredSlots.isEmpty()) {
            player.sendSystemMessage((Component)Component.literal((String)"You perished in the dungeon! All items lost.").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD}));
        }
        // Record analytics for admin Dungeon Analytics tab (death)
        com.ultra.megamod.feature.computer.network.handlers.DungeonAnalyticsHandler.recordRun(
                player.getGameProfile().name(), player.getUUID().toString(),
                instance.tier.getDisplayName(), instance.theme.getDisplayName(),
                "Died", instance.startTimeMs, System.currentTimeMillis(), "");

        manager.removePlayerFromDungeonOnDeath(player);
        removePlayerFromAllBossBars(player);
        PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new DungeonSyncPayload("", "", "", 0, 0, false), (CustomPacketPayload[])new CustomPacketPayload[0]);
        DimensionHelper.clearReturnPosition(player.getUUID());
        // Check if this was the last player — if so, cleanup immediately
        if (!manager.hasRemainingPlayersInDungeon(instance.instanceId, overworld)) {
            manager.cleanupDungeon(instance.instanceId, overworld);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        long gameTime = overworld.getGameTime();

        // Check insurance session timeouts every second (20 ticks)
        if (gameTime % 20L == 0L) {
            InsuranceManager.tickSessions(overworld);
        }

        if (gameTime % 1200L != 0L) {
            return;
        }
        DungeonManager manager = DungeonManager.get(overworld);
        manager.saveToDisk(overworld);
        manager.cleanupExpiredDungeons(overworld, gameTime);
        DungeonLeaderboardManager.get(overworld).saveToDisk(overworld);
        PocketManager.get(overworld).saveToDisk(overworld);
        DimensionHelper.saveToDisk(overworld);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        ServerLevel overworld = player2.level().getServer().overworld();
        DimensionHelper.loadFromDisk(overworld); // no-ops if already loaded
        DungeonManager manager = DungeonManager.get(overworld);
        DungeonManager.DungeonInstance instance = manager.getDungeonForPlayer(player2.getUUID());
        if (instance != null) {
            manager.unmarkAbandoned(player2.getUUID());
            if (player2.level().dimension().equals(MegaModDimensions.DUNGEON)) {
                manager.syncToPlayer(player2, instance);
                player2.sendSystemMessage((Component)Component.literal((String)"Reconnected to your dungeon run.").withStyle(ChatFormatting.GRAY));
            } else {
                manager.cleanupDungeon(instance.instanceId, overworld);
                PacketDistributor.sendToPlayer((ServerPlayer)player2, (CustomPacketPayload)new DungeonSyncPayload("", "", "", 0, 0, false), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        ServerLevel overworld = player2.level().getServer().overworld();
        DungeonManager manager = DungeonManager.get(overworld);
        // Cancel any pending insurance session
        InsuranceManager.onPlayerLogout(player2.getUUID(), overworld);

        if (manager.isPlayerInDungeon(player2.getUUID())) {
            manager.markAbandoned(player2.getUUID(), overworld.getGameTime());
            MegaMod.LOGGER.info("Player {} logged out with active dungeon \u2014 marked as abandoned", (Object)player2.getGameProfile().name());
        }
    }

    @SubscribeEvent
    public static void onBlockInteractInDungeon(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel overworld = player.level().getServer().overworld();
        DungeonManager manager = DungeonManager.get(overworld);
        if (!manager.isPlayerInDungeon(player.getUUID())) return;
        if (AdminSystem.isAdmin(player) && FeatureToggleManager.get(overworld).isEnabled("admin_dungeon_bypass")) return;

        BlockState state = player.level().getBlockState(event.getPos());
        Block block = state.getBlock();
        if (block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL
                || block == Blocks.DISPENSER || block == Blocks.DROPPER) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("You can't use this here.").withStyle(ChatFormatting.GRAY));
        }
    }

    private static void restoreItemToSlot(ServerPlayer player, String slotName, ItemStack stack, ServerLevel overworld) {
        if (slotName.startsWith("armor_")) {
            String equipName = slotName.substring(6);
            EquipmentSlot equipSlot = EquipmentSlot.byName(equipName);
            player.setItemSlot(equipSlot, stack);
        } else if (slotName.equals("mainhand")) {
            // Find the first empty hotbar slot, or slot 0
            Inventory inv = player.getInventory();
            for (int i = 0; i < 9; i++) {
                if (inv.getItem(i).isEmpty()) {
                    inv.setItem(i, stack);
                    return;
                }
            }
            inv.setItem(0, stack);
        } else if (slotName.equals("offhand")) {
            player.setItemSlot(EquipmentSlot.OFFHAND, stack);
        } else if (slotName.startsWith("accessory_")) {
            String accName = slotName.substring(10);
            try {
                AccessorySlotType slotType = AccessorySlotType.valueOf(accName);
                LibAccessoryLookup.setEquipped(player, slotType, stack);
                LibAccessoryLookup.syncToClient(player);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    /** Remove a player from all boss bars in the dungeon dimension */
    private static void removePlayerFromAllBossBars(ServerPlayer player) {
        for (ServerLevel level : player.level().getServer().getAllLevels()) {
            if (!level.dimension().equals(MegaModDimensions.DUNGEON)) continue;
            for (DungeonBossEntity boss : level.getEntitiesOfClass(DungeonBossEntity.class,
                    new net.minecraft.world.phys.AABB(player.blockPosition()).inflate(200.0), e -> true)) {
                boss.removeBossBarPlayer(player);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        DungeonManager manager = DungeonManager.get(overworld);
        manager.saveToDisk(overworld);
        DungeonManager.reset();
        DungeonLeaderboardManager.get(overworld).saveToDisk(overworld);
        DungeonLeaderboardManager.reset();
        PocketManager.get(overworld).saveToDisk(overworld);
        PocketManager.reset();
        DimensionHelper.saveToDisk(overworld);
        DimensionHelper.reset();
    }
}

