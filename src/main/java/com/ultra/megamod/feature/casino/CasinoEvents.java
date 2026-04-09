package com.ultra.megamod.feature.casino;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.casino.blackjack.BlackjackTable;
import com.ultra.megamod.feature.casino.blackjack.BlackjackTableBlockEntity;
import com.ultra.megamod.feature.casino.chips.ChipManager;
import com.ultra.megamod.feature.casino.slots.SlotMachineBlockEntity;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = MegaMod.MODID)
public class CasinoEvents {

    // Seats queued for removal — discard() can't be called during EntityMountEvent
    // because LivingEntity.remove() ejects passengers mid-dismount, causing a crash.
    private static final List<Entity> PENDING_SEAT_CLEANUP = new ArrayList<>();

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        autoCashout(player); // cash out chips before cleanup
        cleanupPlayer(player);
    }

    /**
     * When a player dismounts (shift to get off chair), leave the blackjack table
     * and clean up casino chair ArmorStands.
     */
    @SubscribeEvent
    public static void onDismount(EntityMountEvent event) {
        if (event.isMounting()) return;
        Entity rider = event.getEntityMounting();
        if (!(rider instanceof ServerPlayer player)) return;

        UUID playerId = player.getUUID();

        // Queue the casino seat ArmorStand for removal on the next tick.
        // Cannot call discard() here — LivingEntity.remove() ejects passengers
        // mid-dismount, causing a crash.
        Entity mount = event.getEntityBeingMounted();
        if (mount instanceof net.minecraft.world.entity.decoration.ArmorStand stand
                && stand.getTags().contains("megamod_casino_seat")) {
            PENDING_SEAT_CLEANUP.add(stand);
        }

        // Leave blackjack table on dismount
        BlockPos tablePos = BlackjackTableBlockEntity.getTableForPlayer(playerId);
        if (tablePos != null) {
            net.minecraft.server.MinecraftServer server = player.level().getServer();
            if (server != null) {
                for (ServerLevel worldLevel : server.getAllLevels()) {
                    BlockEntity be = worldLevel.getBlockEntity(tablePos);
                    if (be instanceof BlackjackTableBlockEntity tableBE) {
                        BlackjackTable game = tableBE.getGame();
                        if (game != null && game.isPlayerSeated(playerId)) {
                            game.leaveTable(playerId);
                            player.displayClientMessage(
                                    net.minecraft.network.chat.Component.literal("You left the blackjack table.")
                                            .withStyle(net.minecraft.ChatFormatting.YELLOW), true);
                            break;
                        }
                    }
                }
            }
            BlackjackTableBlockEntity.untrackPlayer(playerId);
        }
    }

    /**
     * Auto-cashout chips when player changes dimension (leaving casino).
     */
    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Check if player LEFT the casino dimension
        if (event.getFrom().equals(com.ultra.megamod.feature.dimensions.MegaModDimensions.CASINO) &&
                !event.getTo().equals(com.ultra.megamod.feature.dimensions.MegaModDimensions.CASINO)) {
            autoCashout(player);
        }
    }

    private static void autoCashout(ServerPlayer player) {
        ChipManager chips = ChipManager.get(player.level().getServer().overworld());
        if (chips.hasAnyChips(player.getUUID())) {
            ServerLevel overworld = player.level().getServer().overworld();
            EconomyManager eco = EconomyManager.get(overworld);
            int cashed = chips.cashOutAll(player.getUUID(), eco);
            if (cashed > 0) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "Casino chips auto-cashed out: +" + cashed + " MC to wallet")
                        .withStyle(net.minecraft.ChatFormatting.GREEN));
            }
            // Send zero chip sync so client display resets
            com.ultra.megamod.feature.casino.chips.ChipActionPayload.sendChipSync(player, eco);
        }
    }

    /**
     * Tick all active roulette games. Without this, roulette never advances
     * from BETTING → SPINNING → RESULT.
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // Clean up seat ArmorStands queued during dismount events
        if (!PENDING_SEAT_CLEANUP.isEmpty()) {
            PENDING_SEAT_CLEANUP.forEach(Entity::discard);
            PENDING_SEAT_CLEANUP.clear();
        }

        ServerLevel overworld = event.getServer().overworld();

        // Use the casino dimension for roulette ticks so broadcasts reach casino players
        ServerLevel casinoLevel = (ServerLevel) event.getServer().getLevel(
                com.ultra.megamod.feature.dimensions.MegaModDimensions.CASINO);
        ServerLevel rouletteLevel = casinoLevel != null ? casinoLevel : overworld;

        // Tick all active roulette games
        for (Map.Entry<BlockPos, com.ultra.megamod.feature.casino.roulette.RouletteGame> entry :
                com.ultra.megamod.feature.casino.network.RouletteActionPayload.RouletteTableRegistry.allGames()) {
            entry.getValue().tick(rouletteLevel);
        }

        // Tick baccarat games for delayed chat messages
        ServerLevel baccaratLevel = casinoLevel != null ? casinoLevel : overworld;
        for (com.ultra.megamod.feature.casino.baccarat.BaccaratGame game :
                com.ultra.megamod.feature.casino.network.BaccaratActionPayload.allGames()) {
            game.tick(baccaratLevel);
        }

        // Periodic chip balance sync to all players in casino (every 60 ticks = 3s)
        long gameTime = overworld.getGameTime();
        if (gameTime % 60L == 0L) {
            EconomyManager eco = EconomyManager.get(overworld);
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                if (ChipManager.get(overworld).hasAnyChips(player.getUUID())) {
                    com.ultra.megamod.feature.casino.chips.ChipActionPayload.sendChipSync(player, eco);
                }
            }
        }

        // Periodic save for casino stats and chips (every 60 seconds = 1200 ticks)
        if (gameTime % 1200L == 0L) {
            CasinoManager.get(overworld).saveToDisk(overworld);
            ChipManager.get(overworld).saveToDisk(overworld);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        CasinoManager.get(overworld).saveToDisk(overworld);
        ChipManager.get(overworld).saveToDisk(overworld);
        CasinoManager.reset();
        ChipManager.reset();
    }

    private static void cleanupPlayer(ServerPlayer player) {
        UUID playerId = player.getUUID();
        net.minecraft.server.MinecraftServer server = player.level().getServer();
        if (server == null) return;

        // Release slot machines
        BlockPos machinePos = SlotMachineBlockEntity.getMachineForPlayer(playerId);
        if (machinePos != null) {
            for (ServerLevel worldLevel : server.getAllLevels()) {
                BlockEntity be = worldLevel.getBlockEntity(machinePos);
                if (be instanceof SlotMachineBlockEntity slotBE && slotBE.isUsedBy(playerId)) {
                    slotBE.release();
                    break;
                }
            }
            SlotMachineBlockEntity.removePlayerTracking(playerId);
        }

        // Leave blackjack tables
        BlockPos tablePos = BlackjackTableBlockEntity.getTableForPlayer(playerId);
        if (tablePos != null) {
            for (ServerLevel worldLevel : server.getAllLevels()) {
                BlockEntity be = worldLevel.getBlockEntity(tablePos);
                if (be instanceof BlackjackTableBlockEntity tableBE) {
                    BlackjackTable game = tableBE.getGame();
                    if (game != null && game.isPlayerSeated(playerId)) {
                        game.leaveTable(playerId);
                        break;
                    }
                }
            }
            BlackjackTableBlockEntity.untrackPlayer(playerId);
        }
    }
}
