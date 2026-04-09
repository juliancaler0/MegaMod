package com.ultra.megamod.feature.combat;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass;
import com.ultra.megamod.feature.combat.network.ClassSelectionPayload;
import com.ultra.megamod.feature.combat.network.ClassSyncPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles class selection triggers on player login.
 * Sends the ClassSelectionPayload to players who haven't chosen a class yet,
 * with a short delay so the world loads before the screen opens.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class ClassEventHandler {

    /** Players awaiting their class selection prompt, mapped to the tick they joined. */
    private static final Map<UUID, Long> PENDING_SELECTION = new ConcurrentHashMap<>();

    /** Delay in ticks before showing the class selection screen (3 seconds). */
    private static final int SELECTION_DELAY_TICKS = 60;

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ServerLevel level = (ServerLevel) player.level();
        PlayerClassManager manager = PlayerClassManager.get(level);

        if (!manager.hasChosenClass(player.getUUID())) {
            // Queue the selection prompt with a delay so the world loads first
            PENDING_SELECTION.put(player.getUUID(), level.getGameTime());
        } else {
            // Player already has a class — sync it to the client for HUD/tooltip display
            PlayerClass cls = manager.getPlayerClass(player.getUUID());
            PacketDistributor.sendToPlayer(player, new ClassSyncPayload(cls.name()));

            // Reapply class-specific prestige attribute bonuses (transient modifiers lost on relog)
            com.ultra.megamod.feature.skills.prestige.PrestigeClassBonusHandler.applyClassPrestigeBonuses(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        PENDING_SELECTION.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (PENDING_SELECTION.isEmpty()) return;

        var server = event.getServer();
        ServerLevel overworld = server.overworld();
        long currentTick = overworld.getGameTime();

        var iterator = PENDING_SELECTION.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            UUID playerId = entry.getKey();
            long joinTick = entry.getValue();

            if (currentTick - joinTick < SELECTION_DELAY_TICKS) continue;

            // Time to send the prompt
            iterator.remove();
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player == null) continue;

            // Double-check they still haven't chosen (could have been set by admin)
            PlayerClassManager manager = PlayerClassManager.get(overworld);
            if (!manager.hasChosenClass(playerId)) {
                PacketDistributor.sendToPlayer(player, new ClassSelectionPayload());
            }
        }
    }

    /** Called on server stop to clean up static state. */
    public static void clearAll() {
        PENDING_SELECTION.clear();
    }
}
