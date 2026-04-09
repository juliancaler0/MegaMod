package com.ultra.megamod.feature.hud;

import com.ultra.megamod.feature.hud.network.DeathRecapPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Server-side: tracks recent damage history per player (last 5 seconds).
 * On death, compiles and sends death recap to the client.
 */
@EventBusSubscriber(modid = "megamod")
public class DamageHistoryTracker {

    private static final Map<UUID, Deque<DamageRecord>> HISTORY = new HashMap<>();
    private static final int MAX_HISTORY_TICKS = 100; // 5 seconds

    public static void record(ServerPlayer player, String sourceName, String damageType, float amount) {
        int tick = player.level().getServer().getTickCount();
        Deque<DamageRecord> deque = HISTORY.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>());
        deque.addLast(new DamageRecord(sourceName, damageType, amount, tick));
        // Trim old entries
        while (!deque.isEmpty() && tick - deque.peekFirst().tick > MAX_HISTORY_TICKS) {
            deque.pollFirst();
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        UUID uuid = sp.getUUID();
        Deque<DamageRecord> deque = HISTORY.get(uuid);

        // Determine killer
        String killerName = "Unknown";
        String killerType = "unknown";
        if (event.getSource().getEntity() != null) {
            killerName = event.getSource().getEntity().getName().getString();
            killerType = event.getSource().getEntity().getType().toShortString();
        } else {
            killerType = event.getSource().type().msgId();
            killerName = killerType; // e.g., "fall", "lava", "outOfWorld"
        }

        // Build recap
        List<DeathRecapPayload.DamageEntry> entries = new ArrayList<>();
        int currentTick = sp.level().getServer().getTickCount();

        if (deque != null) {
            for (DamageRecord rec : deque) {
                int ticksAgo = currentTick - rec.tick;
                entries.add(new DeathRecapPayload.DamageEntry(rec.source, rec.type, rec.amount, ticksAgo));
            }
            deque.clear();
        }

        PacketDistributor.sendToPlayer(sp, new DeathRecapPayload(entries, killerName, killerType));
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        HISTORY.remove(event.getEntity().getUUID());
    }

    private record DamageRecord(String source, String type, float amount, int tick) {}
}
