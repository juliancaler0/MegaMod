package com.ultra.megamod.feature.baritone;

import com.ultra.megamod.MegaMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton manager for all bot instances.
 * Ticks all active bots on each server tick.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class BotManager {
    private static final Map<UUID, BotInstance> bots = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // Remove bots for disconnected players
        bots.entrySet().removeIf(entry -> {
            ServerPlayer p = entry.getValue().getPlayer();
            return p == null || p.isRemoved();
        });
        for (BotInstance bot : bots.values()) {
            try {
                bot.tick();
            } catch (Exception e) {
                String name = bot.getPlayer() != null ? bot.getPlayer().getGameProfile().name() : "unknown";
                MegaMod.LOGGER.error("Bot tick error for {}: {}", name, e.getMessage());
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        removeAll();
    }

    /**
     * Get or create a bot instance for a player.
     * If a stale instance exists (old player object), it is replaced.
     */
    public static BotInstance getOrCreate(ServerPlayer player) {
        UUID uuid = player.getUUID();
        BotInstance existing = bots.get(uuid);
        if (existing != null && !existing.getPlayer().isRemoved()) {
            return existing;
        }
        // Create fresh instance (handles reconnects with new ServerPlayer object)
        BotInstance bot = new BotInstance(player);
        bots.put(uuid, bot);
        return bot;
    }

    public static BotInstance get(UUID uuid) {
        return bots.get(uuid);
    }

    public static void remove(UUID uuid) {
        BotInstance bot = bots.remove(uuid);
        if (bot != null) {
            bot.getProcessManager().cancelAll();
        }
    }

    public static Map<UUID, BotInstance> getAllBots() {
        return bots;
    }

    public static void removeAll() {
        for (BotInstance bot : bots.values()) {
            bot.getProcessManager().cancelAll();
        }
        bots.clear();
    }
}
